package ai.utiliti.bes.tasks;

import ai.utiliti.bes.BlockStatus;
import ai.utiliti.bes.model.BlockchainEvent;
import ai.utiliti.bes.model.EventJob;
import ai.utiliti.bes.model.WrappedBlockHeader;
import ai.utiliti.bes.respository.BlockchainEventRepository;
import ai.utiliti.bes.respository.WrappedBlockRepository;
import ai.utiliti.bes.services.SQSService;
import ai.utiliti.bes.util.InternalApiUtil;
import ai.utiliti.bes.webhooks.ContractEvent;
import ai.utiliti.bes.webhooks.WebhookContractSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

@Component
@Profile("network-data")
public class EventConfirmationTask {

    private final Logger logger = LoggerFactory.getLogger(EventConfirmationTask.class);

    private final BlockchainEventRepository blockchainEventRepository;
    private final WrappedBlockRepository wrappedBlockRepository;
    private final SQSService sqsService;

    @Autowired
    public EventConfirmationTask(BlockchainEventRepository blockchainEventRepository, WrappedBlockRepository wrappedBlockRepository, SQSService sqsService) {
        this.blockchainEventRepository = blockchainEventRepository;
        this.wrappedBlockRepository = wrappedBlockRepository;
        this.sqsService = sqsService;
    }

    /**
     * This function checks the latest processed block, and checks all events which are
     * pending and have passed the block confirmation threshold for the given network
     * then confirms them and sends out new event logs with updated status.
     * Runs every 1 second
     */
    @Scheduled(fixedDelay = 100)
    public void checkEventFinality() {
        try {

            WrappedBlockHeader lastProcessedBlock = wrappedBlockRepository.findFirstByNetworkOrderByNumberDesc(Integer.parseInt(System.getenv("NETWORK_ID"))).orElse(null);

            if (lastProcessedBlock == null) return; // If we have no blocks processed we cannot confirm anything

            BigInteger blocksMustBeOlderThan = lastProcessedBlock.getNumber().subtract(BigInteger.valueOf(Long.parseLong(System.getenv("NETWORK_CONFIRMATION_THRESHOLD"))));

            logger.info("Checking confirmations for blocks older than {}", blocksMustBeOlderThan.intValue());

            int maxPageSize = 1000;

            Pageable pageRequest = PageRequest.of(0, maxPageSize);
            Page<BlockchainEvent> page = blockchainEventRepository.findAllByStatusAndBlockNumberLessThanEqual(BlockStatus.PENDING.name(), blocksMustBeOlderThan, PageRequest.of(0, maxPageSize));

            logger.info("pending {}", page.getTotalElements());
            List<WebhookContractSubscription> subscriptions = InternalApiUtil.getWebhookSubscriptions();

            Map<String, Pair<String, String>> webhookInfo = new HashMap<>();

            for (WebhookContractSubscription subscription : subscriptions) {
                webhookInfo.putIfAbsent(subscription.getWebhookId(), Pair.of(subscription.getWebhookSecret(), subscription.getWebhookUrl()));
            }

            while (!page.isEmpty()) {
                List<EventJob> eventJobs = new ArrayList<>();

                logger.info("Continuing block confirmations before block {}", blocksMustBeOlderThan.intValue());
                for (BlockchainEvent event : page.getContent()) {
                    event.setStatus(BlockStatus.CONFIRMED.name());

                    if (event.getWebhookId() == null || webhookInfo.get(event.getWebhookId()) == null) continue;

                    eventJobs.add(new EventJob(
                            event,
                            new ContractEvent(
                                    event.getWebhookId(),
                                    webhookInfo.get(event.getWebhookId()).getFirst(),
                                    webhookInfo.get(event.getWebhookId()).getSecond(),
                                    event.getContractId()
                            ),
                            BlockStatus.CONFIRMED
                    ));
                }
                blockchainEventRepository.saveAll(page.getContent());

                eventJobs.forEach(sqsService::sendEventJobToSQS);
                pageRequest = pageRequest.next();
                page = blockchainEventRepository.findAllByStatusAndBlockNumberLessThanEqual(BlockStatus.PENDING.name(), blocksMustBeOlderThan, pageRequest);
            }
        
            logger.info("Finished confirming blocks older than {}!", blocksMustBeOlderThan.intValue());
        } catch (Exception e) {
            System.out.println("Error running confirmation task");
            e.printStackTrace();
        }
    }
}

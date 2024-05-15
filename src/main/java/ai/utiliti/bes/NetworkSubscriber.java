package ai.utiliti.bes;

import ai.utiliti.bes.model.WrappedBlockHeader;
import ai.utiliti.bes.services.SQSService;
import com.google.gson.Gson;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;

import javax.annotation.PostConstruct;


@Component
@Profile("network-data")
public class NetworkSubscriber {

    private final int networkId;
    private final int networkConfirmationThreshold;
    private final SQSService sqsService;

    private final Logger logger = LoggerFactory.getLogger(NetworkSubscriber.class);

    private final Web3j web3j;

    @Autowired
    public NetworkSubscriber(SQSService sqsService, Web3j web3j) {
        this.sqsService = sqsService;
        this.networkId = Integer.parseInt(System.getenv("NETWORK_ID"));
        this.networkConfirmationThreshold = Integer.parseInt(System.getenv("NETWORK_CONFIRMATION_THRESHOLD"));
        this.web3j = web3j;
    }

    @PostConstruct
    public void subscribeToBlockHeaders() {

        logger.info("Subscribing to new block headers");


        Disposable blockSubscriptionDisposable = web3j.blockFlowable(false).subscribe(blockHeader -> {
            if (blockHeader.getBlock() == null) {
                return;
            }

            WrappedBlockHeader wrappedBlockHeader = new WrappedBlockHeader(blockHeader.getBlock());
            this.sqsService.sendObjectToQueue(System.getenv("BLOCK_QUEUE_URL"), wrappedBlockHeader);

        }, Throwable::printStackTrace);
    }
}
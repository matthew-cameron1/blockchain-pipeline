package ai.utiliti.bes;

import ai.utiliti.bes.model.BlockchainEvent;
import ai.utiliti.bes.model.EventJob;
import ai.utiliti.bes.services.SQSService;
import ai.utiliti.bes.tasks.EventLogSqsTask;
import ai.utiliti.bes.util.EventParameter;
import ai.utiliti.bes.webhooks.ContractEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Block;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@ContextConfiguration(classes = TestConfiguration.class)
@Import(value = {TestConfiguration.class})
@ExtendWith(value = SpringExtension.class)
public class TestEventJobWorkers {

    @Autowired
    private SQSService sqsService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4); // Just for testing purposes

    @Test
    public void eventLogsEmittedCorrectSize() throws InterruptedException, ExecutionException {
        List<EventJob> jobs = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            jobs.add(Mockito.mock(EventJob.class));

//            BlockchainEvent event = new BlockchainEvent(
//                    "Transfer",
//                    Date.from(Instant.now()),
//                    new EventParameter[]{},
//                    "0x0",
//                    "0x0",
//                    BigInteger.ONE,
//                    BlockStatus.PENDING.name(),
//                    BigInteger.ONE,
//                    137,
//                    UUID.randomUUID().toString(),
//                    UUID.randomUUID().toString()
//            );
//            jobs.add(new EventJob(event, new ContractEvent("", "", "", ""), BlockStatus.PENDING));
        }

        List<Future<Integer>> futures = batchAndSendEventJobs(jobs);

        int total = 0;
        for (Future<Integer> future : futures) {
            total += future.get();
        }
        Assertions.assertEquals(jobs.size(), total);
    }

    private List<Future<Integer>> batchAndSendEventJobs(List<EventJob> jobs) throws InterruptedException {
        List<Pair<Integer, Integer>> eventLogBatchIndecies = createTaskBatches(0, jobs.size(), 4);

        List<EventLogSqsTask> eventLogJobFutures = new ArrayList<>();

        for (Pair<Integer, Integer> batch : eventLogBatchIndecies) {
            List<EventJob> jobsForTask = jobs.subList(batch.getFirst(), batch.getSecond() + 1);
            eventLogJobFutures.add(new EventLogSqsTask(eventLogBatchIndecies.indexOf(batch), jobsForTask, this.sqsService));
        }
        return executorService.invokeAll(eventLogJobFutures);
    }

    private List<Pair<Integer, Integer>> createTaskBatches(int start, int amount, int workers) {
        int batchSize = (int) Math.floor(amount / workers);
        int remainder = amount % workers;

        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            int startNum = start + i * batchSize;
            int endNum = startNum + batchSize - 1;
            if (i == workers - 1) {
                endNum += remainder;
            }
            pairs.add(Pair.of(startNum, endNum));
        }
        return pairs;
    }
}
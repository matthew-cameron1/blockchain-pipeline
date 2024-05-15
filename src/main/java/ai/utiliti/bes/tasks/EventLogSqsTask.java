package ai.utiliti.bes.tasks;

import ai.utiliti.bes.model.EventJob;
import ai.utiliti.bes.services.SQSService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

@Getter
@Setter
@RequiredArgsConstructor
public class EventLogSqsTask implements Callable<Integer> {
    
    private final int id;
    private final List<EventJob> jobs;
    private final SQSService sqsService;
    
    private final Logger logger = LoggerFactory.getLogger(EventLogSqsTask.class);
    
    @Override
    public Integer call() throws Exception {
        int count = 0;
        
        for (EventJob job : jobs) {
            count++;
            sqsService.sendEventJobToSQS(job);
            
            if (count % 5 == 0) {
                double percent = (double) count / jobs.size() * 100;
                logger.info("Event Log Task {} - {}% complete", id, String.format("%.2f", percent));
            }
        }
        return count;
    }
}
package ai.utiliti.bes.consumers;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.cloud.aws.messaging.listener.Visibility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Getter
@Setter
@RequiredArgsConstructor
public class ConsumerVisibilityExtender {
    
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> visibilityFuture;
    
    public void start(Visibility visibility, int interval, TimeUnit unit) {
        this.visibilityFuture = executorService.schedule(() -> {
            visibility.extend(interval + 1);
        }, interval, unit);
    }
    
    public void stop() {
        if (visibilityFuture == null || visibilityFuture.isCancelled()) return;
        
        visibilityFuture.cancel(true);
    }
}
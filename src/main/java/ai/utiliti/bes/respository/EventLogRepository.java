package ai.utiliti.bes.respository;

import ai.utiliti.bes.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventLogRepository extends JpaRepository<EventLog, UUID> {
    
}
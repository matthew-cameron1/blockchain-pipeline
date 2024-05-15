package ai.utiliti.bes.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

public class PushNotification {
  
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "pg-uuid")
    private UUID id;
    
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String payload;
}
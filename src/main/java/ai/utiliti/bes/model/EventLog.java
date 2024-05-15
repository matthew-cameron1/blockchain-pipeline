package ai.utiliti.bes.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import ai.utiliti.bes.model.converters.UUIDStringConverter;
import ai.utiliti.bes.util.GsonExclude;
import io.reactivex.annotations.Nullable;

@Entity
@Table(name="\"EventLog\"", indexes = {
    @Index(columnList = "\"blockchainEventId\""),
    @Index(columnList = "webhook_id")
})
@TypeDef(name="jsonb", typeClass = JsonBinaryType.class)
public class EventLog implements Serializable {

    @Id
    @GeneratedValue
    @Convert(converter = UUIDStringConverter.class)
    private UUID id;

    @Column(name = "webhook_id")
    private String webhookId;

    @Column(name = "contract_id")
    private String contractId;

    @ManyToOne
    @JoinColumn(name = "\"blockchainEventId\"")
    @GsonExclude
    private BlockchainEvent blockchainEvent;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String request;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String response;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "event_name")
    private String eventName;

    private String status;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String error;

    public EventLog(String webhookId, String contractId, BlockchainEvent event, String request, String response, @Nullable Integer responseCode, String eventName, String status, @Nullable String error) {
        this.webhookId = webhookId;
        this.contractId = contractId;
        this.blockchainEvent = event;
        this.request = request;
        this.response = response;
        this.responseCode = responseCode;
        this.eventName = eventName;
        this.status = status;
        this.error = error;
    }

    protected EventLog() {}
}
package ai.utiliti.bes.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import com.google.gson.annotations.SerializedName;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import ai.utiliti.bes.util.EventParameter;
import lombok.Getter;
import lombok.Setter;

@Table(name="\"BlockchainEvent\"", indexes = {
    @Index(name = "eventId", columnList = "\"transactionHash\", \"logIndex\", webhook_id", unique = true),
    @Index(columnList = "status"),
    @Index(columnList = "\"blockNumber\"")
})
@Entity
@Getter
@Setter
@TypeDef(name="jsonb", typeClass = JsonBinaryType.class)
public class BlockchainEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "text", updatable = false)
    @Type(type = "pg-uuid")
    private UUID id;

    @Column(name = "\"eventName\"")
    private String eventName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"timeStamp\"")
    private Date timestamp;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "\"eventParameters\"")
    private EventParameter[] eventParameters;

    @Column(name = "\"contractAddress\"")
    private String contractAddress;

    @OneToMany(mappedBy = "blockchainEvent")
    private Set<EventLog> eventLogs;

    @Column(name = "\"transactionHash\"")
    private String transactionHash;

    @Column(name = "\"blockNumber\"")
    private BigInteger blockNumber;
    private String status;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"")
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"updatedAt\"")
    private Date updatedAt;

    @Column(name = "\"logIndex\"")
    private BigInteger logIndex;
    private Integer network;

    @Column(name = "webhook_id")
    @SerializedName("webhook_id")
    private String webhookId;

    @Column(name = "contract_id")
    @SerializedName("contract_id")
    private String contractId;

    protected BlockchainEvent() {}

    public BlockchainEvent(String eventName, Date timestamp, EventParameter[] eventParameters,
                           String contractAddress, String transactionHash, BigInteger blockNumber,
                           String status, BigInteger logIndex, Integer network, String webhookId, String contractId) {
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.eventParameters = eventParameters;
        this.contractAddress = contractAddress;
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
        this.status = status;
        this.logIndex = logIndex;
        this.network = network;
        this.webhookId = webhookId;
        this.contractId = contractId;
    }
}
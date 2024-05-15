package ai.utiliti.bes.model.opensea;


import ai.utiliti.bes.model.ListingStatus;
import ai.utiliti.bes.model.Marketplace;
import io.reactivex.annotations.Nullable;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "marketplace_listings",
        indexes = {
                @Index(columnList = "marketplace"),
                @Index(columnList = "order_hash"),
                @Index(columnList = "event_name"),
                @Index(columnList = "collection"),
                @Index(columnList = "listing_amount"),
                @Index(columnList = "expiration_date"),
                @Index(columnList = "collection,marketplace,status,expiration_date")
        }
)
public class MarketplaceListing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "order_hash", unique = true)
    private String orderHash;

    @Column(name = "event_name")
    private String eventName;
    private String collection;

    @Enumerated(EnumType.STRING)
    private Marketplace marketplace;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    @Column(name = "listing_amount", columnDefinition = "NUMERIC")
    private BigInteger listingAmount;

    @Nullable
    private String tokenId;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiration_date")
    private Date expirationDate;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}

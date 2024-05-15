package ai.utiliti.bes.model.opensea.payloads;

import ai.utiliti.bes.model.opensea.data.*;
import com.google.gson.annotations.SerializedName;
import io.reactivex.annotations.Nullable;
import lombok.Data;

import java.util.Date;

@Data
public class ItemReceivedOfferPayload {

    @SerializedName("base_price")
    private String basePrice;
    private Collection collection;

    @SerializedName("created_data")
    private Date createdDate;

    @SerializedName("event_timestamp")
    private Date eventTimestamp;

    @SerializedName("expiration_date")
    private Date expirationDate;

    private Item item;

    private Address maker;

    @SerializedName("order_hash")
    private String orderHash;

    @SerializedName("payment_token")
    private Token paymentToken;

    @SerializedName("protocol_data")
    private ProtocolData protocolData;

    private int quantity;

    @Nullable
    private Address taker;
}
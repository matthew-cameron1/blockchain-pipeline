package ai.utiliti.bes.model.opensea.payloads;

import ai.utiliti.bes.model.opensea.data.*;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class ItemSoldPayload {

    @SerializedName("closing_date")
    private Date closingDate;
    private Collection collection;

    @SerializedName("event_timestamp")
    private Date eventTimestamp;
    private Item item;

    private Address maker;

    @SerializedName("order_hash")
    private String orderHash;
    @SerializedName("payment_token")
    private Token paymentToken;

    @SerializedName("protocol_data")
    private ProtocolData protocolData;

    private int quantity;

    @SerializedName("sale_price")
    private String salePrice;
    
    private Address taker;
    private Transaction transaction;
}
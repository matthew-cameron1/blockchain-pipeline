package ai.utiliti.bes.model.opensea.payloads;

import ai.utiliti.bes.model.opensea.data.*;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class ItemListedPayload {
    
    @SerializedName("base_price")
    private String basePrice;
    private Collection collection;
    private Item item;

    @SerializedName("listing_date")
    private Date listingDate;

    @SerializedName("expiration_date")
    private Date expirationDate;

    @SerializedName("listing_type")
    private String listingType;
    private Address maker;
    
    @SerializedName("order_hash")
    private String orderHash;

    @SerializedName("payment_token")
    private Token paymentToken;

    @SerializedName("protocol_data")
    private ProtocolData protocolData;
}
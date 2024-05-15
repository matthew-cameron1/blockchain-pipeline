package ai.utiliti.bes.model.opensea.payloads;

import ai.utiliti.bes.model.opensea.data.Address;
import ai.utiliti.bes.model.opensea.data.Collection;
import ai.utiliti.bes.model.opensea.data.Item;
import ai.utiliti.bes.model.opensea.data.Transaction;
import com.google.gson.annotations.SerializedName;
import io.reactivex.annotations.Nullable;
import lombok.Data;

import java.util.Date;

@Data
public class ItemTransferredPayload {

    private Collection collection;

    @SerializedName("event_timestamp")
    private Date eventTimestamp;

    @SerializedName("from_account")
    private Address fromAccount;
    private Item item;

    private int quantity;

    @SerializedName("to_account")
    private Address toAccount;

    @Nullable
    private Transaction transaction;
}
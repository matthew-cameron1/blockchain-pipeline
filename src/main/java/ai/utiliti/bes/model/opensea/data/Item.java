package ai.utiliti.bes.model.opensea.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Item {
    
    private Chain chain;
    private Metadata metadata;

    @SerializedName("nft_id")
    private String nftId;
    private String permalink;
}
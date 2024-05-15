package ai.utiliti.bes.model.opensea.data;

import com.google.gson.annotations.SerializedName;

public class Token {
    private String address;
    private int decimals;

    @SerializedName("eth_price")
    private String ethPrice;
    
    private String name;
    private String symbol;

    @SerializedName("usd_price")
    private String usdPrice;
}
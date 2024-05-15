package ai.utiliti.bes.model.opensea.data;

import com.google.gson.annotations.SerializedName;

public class Trait {
  
    @SerializedName("trait_name")
    private String traitName;
    
    @SerializedName("trait_type")
    private String traitType;
}
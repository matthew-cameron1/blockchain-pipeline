package ai.utiliti.bes.model.opensea.data;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Metadata {
    @SerializedName("animation_url")
    private String animationUrl;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("metadata_url")
    private String metadataUrl;
    
    private String name;
}
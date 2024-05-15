package ai.utiliti.bes.webhooks;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Contracts {
    
    @SerializedName("network_id")
    private final int networkId;

    private final String name;
    private final String id;
    private final String address;
    private final String abi;
}
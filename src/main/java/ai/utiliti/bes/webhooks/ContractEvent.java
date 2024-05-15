package ai.utiliti.bes.webhooks;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
@Setter
public class ContractEvent implements Serializable {

    @SerializedName("webhook_id")
    private final String webhookId;
    @SerializedName("webhook_secret")
    private final String webhookSecret;
    @SerializedName("webhook_url")
    private final String webhookUrl;
    @SerializedName("contract_id")
    private final String contractId;
}
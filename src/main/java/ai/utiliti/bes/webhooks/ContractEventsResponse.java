package ai.utiliti.bes.webhooks;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractEventsResponse {

    @SerializedName("Contracts")
    private Contracts contracts;

    @SerializedName("Webhooks")
    private Webhook webhooks;

    @SerializedName("WebhookSubscriptions")
    private WebhookSubscription webhookSubscriptions;
}
package ai.utiliti.bes.webhooks;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class WebhookSubscription {
    private final String id;
    private final boolean enabled;

    @SerializedName("webhook_id")
    private final String webhookId;

    @SerializedName("contract_id")
    private final String contractId;

    @SerializedName("event_name")
    private final String eventName;
}

package ai.utiliti.bes.webhooks;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class WebhookContractSubscription {
    private final String contractAddress;
    private final String abi;
    private final String eventName;
    private final String contractName;
    private final String webhookUrl;
    private final String webhookSecret;
    private final String webhookId;
    private final String contractId;
}
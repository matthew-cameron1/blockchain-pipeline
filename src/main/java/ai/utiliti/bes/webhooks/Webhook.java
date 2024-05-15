package ai.utiliti.bes.webhooks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class Webhook {
    private final String id;
    private final String url;
    private final String secret;
}


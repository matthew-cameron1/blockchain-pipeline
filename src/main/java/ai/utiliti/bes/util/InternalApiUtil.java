package ai.utiliti.bes.util;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import ai.utiliti.bes.webhooks.ContractEventsResponse;
import ai.utiliti.bes.webhooks.WebhookContractSubscription;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class InternalApiUtil {

    private static Gson gson;

    @Autowired
    public InternalApiUtil(Gson gson) {
        InternalApiUtil.gson = gson;
    }


    public static List<WebhookContractSubscription> getWebhookSubscriptions() {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(Duration.ofSeconds(5)).build();
        String eventsEndpoint = System.getenv("INTERNAL_API_URL") + "/events?network_id=" + System.getenv("NETWORK_ID");

        Request request = new Request.Builder()
            .url(eventsEndpoint)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + System.getenv("INTERNAL_API_KEY"))
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful() || response.body() == null) {
                return Lists.newArrayList();
            }

            ContractEventsResponse[] contractsArray = gson.fromJson(response.body().string(), ContractEventsResponse[].class);

            List<WebhookContractSubscription> webhookSubscriptions = new ArrayList<>();

            for (ContractEventsResponse eventsResponse : contractsArray) {
                if (!eventsResponse.getWebhookSubscriptions().isEnabled()) {
                    continue;
                }

                String eventName = eventsResponse.getWebhookSubscriptions().getEventName();
                String encodeFunctionSignature;

                if (eventName.contains("(")) {
                    encodeFunctionSignature = eventName.substring(0, eventName.indexOf("(") + 1);
                    String paramNamesAndTypes = eventName.substring(eventName.indexOf("(") + 1, eventName.length() - 1);
                    String[] paramNamesAndTypeArray = paramNamesAndTypes.split(",");

                    if (paramNamesAndTypeArray.length > 0) {
                        for (int i = 0; i < paramNamesAndTypeArray.length; i++){
                            String paramNameAndType = paramNamesAndTypeArray[i];

                            if (paramNameAndType.split(":").length < 2) {
                                encodeFunctionSignature += paramNameAndType.replace(":", "").trim();
                            } else {
                                encodeFunctionSignature += paramNameAndType.split(":")[1].trim() + (i == paramNamesAndTypeArray.length - 1 ? ")" : ",");
                            }
                        }
                    } else {
                        encodeFunctionSignature += paramNamesAndTypes.split(":")[1] + ")";
                    }
                    eventName = encodeFunctionSignature;
                }
                webhookSubscriptions.add(new WebhookContractSubscription(
                        eventsResponse.getContracts().getAddress(),
                        eventsResponse.getContracts().getAbi(),
                        eventName,
                        eventsResponse.getContracts().getName(),
                        eventsResponse.getWebhooks().getUrl(),
                        eventsResponse.getWebhooks().getSecret(),
                        eventsResponse.getWebhooks().getId(),
                        eventsResponse.getContracts().getId()
                ));
            }
            return webhookSubscriptions;
        } catch (Exception e) {
            System.out.println("Could not fetch webhook subscriptions!");
        }
        return null;
    }
}

package ai.utiliti.bes.lambda;

import ai.utiliti.bes.model.EventJob;
import ai.utiliti.bes.model.EventLog;
import ai.utiliti.bes.services.LambdaDBService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class EventLogLambdaFunction implements RequestHandler<SQSEvent, String> {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final OkHttpClient client = new OkHttpClient.Builder().callTimeout(Duration.of(3, ChronoUnit.SECONDS)).build();
    private final LambdaDBService lambdaDBService = new LambdaDBService();

    @Override
    public String handleRequest(SQSEvent input, Context context) {

        LambdaLogger logger = context.getLogger();

        String response = "200 OK";

        for (SQSEvent.SQSMessage message : input.getRecords()) {
            try {
                EventJob job = gson.fromJson(message.getBody(), EventJob.class);
                String eventBody = gson.toJson(job.getEvent());

                String webhookSecret = job.getContractEvent().getWebhookSecret();
                String webhookUrl = job.getContractEvent().getWebhookUrl();
                String webhookId = job.getContractEvent().getWebhookId();
                String contractId = job.getContractEvent().getContractId();

                Request request = new Request.Builder()
                    .url(new URL(webhookUrl))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Webhook-Secret", webhookSecret)
                    .post(RequestBody.create(eventBody, MediaType.get("application/json; charset=utf-8")))
                    .build();

                try (Response httpResponse = client.newCall(request).execute()) {

                    EventLog log = new EventLog(
                            webhookId, contractId,
                            job.getEvent(),
                            gson.toJson(request),
                            gson.toJson(httpResponse.headers()),
                            httpResponse.code(),
                            job.getEvent().getEventName(),
                            job.getEvent().getStatus(),
                            null
                    );

                    logger.log("Saving event log");
                    lambdaDBService.save(log);
                    logger.log("Saved log!");
                } catch (Exception e) {
                    EventLog errorEventLog = new EventLog(
                            webhookId, contractId,
                            job.getEvent(),
                            gson.toJson(request),
                            null,
                            null,
                            job.getEvent().getEventName(),
                            job.getEvent().getStatus(),
                            gson.toJson(e)
                    );
                    lambdaDBService.save(errorEventLog);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }
}
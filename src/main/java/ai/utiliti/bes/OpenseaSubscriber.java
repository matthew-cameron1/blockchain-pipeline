package ai.utiliti.bes;

import ai.utiliti.bes.consumers.OpenseaDataConsumer;
import ai.utiliti.bes.model.converters.DateDeserializer;
import ai.utiliti.bes.model.opensea.OpenseaWebsocketMessage;
import com.google.gson.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;

@Component
@Profile("marketplace-data")
public class OpenseaSubscriber {
    
    private final Logger logger = LoggerFactory.getLogger(OpenseaSubscriber.class);
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder().readTimeout(Duration.ofSeconds(5)).build();
    private final String openseaWebSocketUrl = String.format("wss://stream.openseabeta.com/socket/websocket?token=%s", System.getenv("OPENSEA_API_KEY"));
    
    private final Gson gson = new GsonBuilder().setLenient().registerTypeAdapter(Date.class, new DateDeserializer()).create();
    private WebSocket webSocket;
    
    @Autowired
    private OpenseaDataConsumer openseaDataConsumer;
    
    @PostConstruct
    public void subscribe() {
        Request request = new Request.Builder()
            .url(openseaWebSocketUrl)
            .build();
        
        this.webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                try {
                    OpenseaWebsocketMessage message = gson.fromJson(text, OpenseaWebsocketMessage.class);
                    openseaDataConsumer.handle(message);
                } catch (Exception e) {
                    logger.error("Could not process websocket message", e);
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                
                logger.info("Opensea WebSocket opened!");
            }
        });
        
        String subscribeMessage = gson.toJson(new OpenseaWebsocketMessage(
            "collection:*",
            "phx_join",
            new JsonObject(),
            0
        ));
        
        this.webSocket.send(subscribeMessage);
        logger.info("Subscribed to opensea collection events");
    }
    
    @Scheduled(fixedRate = 30000)
    private void heartbeat() {
        try {
            if (webSocket == null) {
                throw new RuntimeException("Cannot send heartbeat to null websocket");
            }
            
            String jsonPayload = gson.toJson(
                new OpenseaWebsocketMessage(
                    "phoenix",
                    "heartbeat",
                    new JsonObject(),
                    0
                )
            );
            
            webSocket.send(jsonPayload);
        } catch (Exception e) {
            logger.error("Could not send heartbeat", e);
        }
    }
}


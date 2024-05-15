package ai.utiliti.bes.model.opensea;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class OpenseaWebsocketMessage {

  private final String topic;
  private final String event;
  private final JsonObject payload;
  private final int ref;
}
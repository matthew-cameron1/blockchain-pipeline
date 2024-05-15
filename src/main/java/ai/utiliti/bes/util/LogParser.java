package ai.utiliti.bes.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.DefaultFunctionReturnDecoder;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthLog;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LogParser {

    private static Gson gson;

    @Autowired
    private Gson autowiredGson;

    @PostConstruct
    public void setupGson() {
        gson = this.autowiredGson;
    }

    public static EventDetail decodeLogToEventDetails(Web3j web3j, String abi, EthLog.LogObject log) {
        JsonArray jsonArray = gson.fromJson(abi, JsonArray.class);
        Stream<JsonElement> events = jsonArray.asList().stream().filter(element -> element.getAsJsonObject().get("type").getAsString().equals("event") && !element.getAsJsonObject().get("anonymous").getAsBoolean());

        Optional<JsonElement> optional = events.filter(e -> {
            Event web3Event = getEventFromJsonElement(e);
            return EventEncoder.encode(web3Event).equals(log.getTopics().get(0));
        }).findFirst();


        if (optional.isEmpty()) {
            return null;
        }

        Event e = getEventFromJsonElement(optional.get());
        JsonObject abiEventObject = optional.get().getAsJsonObject();
        List<JsonElement> indexedInputs = abiEventObject.get("inputs").getAsJsonArray().asList().stream().filter(element -> element.getAsJsonObject().get("indexed").getAsBoolean()).collect(Collectors.toList());
        List<JsonElement> nonIndexedInputs = abiEventObject.get("inputs").getAsJsonArray().asList().stream().filter(element -> !element.getAsJsonObject().get("indexed").getAsBoolean()).collect(Collectors.toList());

        List<EventParameter> parameters = new ArrayList<>();

        List<Type> nonIndexedValues = FunctionReturnDecoder.decode(log.getData(), e.getNonIndexedParameters());

        for (int i = 0; i < e.getIndexedParameters().size(); i++) {
            JsonObject input = indexedInputs.get(i).getAsJsonObject();
            String name = input.get("name").getAsString();
            String type = input.get("type").getAsString();
            Type value = FunctionReturnDecoder.decodeIndexedValue(log.getTopics().get(i + 1), e.getIndexedParameters().get(i));
            parameters.add(new EventParameter(name, type, value.getValue().toString()));
        }

        for (int i = 0; i < nonIndexedValues.size(); i++) {
            JsonObject input = nonIndexedInputs.get(i).getAsJsonObject();
            parameters.add(new EventParameter(input.get("name").getAsString(), input.get("type").getAsString(), nonIndexedValues.get(i).getValue().toString()));
        }

        String eventSignature = e.getName() + "(";
        for (int i = 0; i < parameters.size(); i++) {
            eventSignature += parameters.get(i).getType() + (i == parameters.size() - 1 ? ")" : ",");
        }

        return new EventDetail(eventSignature, parameters.toArray(new EventParameter[0]));
    }

    public static Event getEventFromJsonElement(JsonElement element) {
        Stream<TypeReference<Type>> stream = element.getAsJsonObject().get("inputs").getAsJsonArray().asList().stream().map(input -> {
            try {
                return TypeReference.makeTypeReference(input.getAsJsonObject().get("type").getAsString(), input.getAsJsonObject().get("indexed").getAsBoolean(), false);
            } catch (ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }
        });

        return new Event(element.getAsJsonObject().get("name").getAsString(), stream.collect(Collectors.toList()));
    }
}
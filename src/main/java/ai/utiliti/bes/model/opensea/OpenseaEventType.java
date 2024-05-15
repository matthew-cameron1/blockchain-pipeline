package ai.utiliti.bes.model.opensea;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OpenseaEventType {
    ITEM_LISTED,
    ITEM_SOLD,
    ITEM_TRANSFERRED,
    ITEM_RECEIVED_OFFER,
    ITEM_RECEIVED_BID,
    COLLECTION_OFFER,
    TRAIT_OFFER;

    private static final Map<String, OpenseaEventType> NAMES = Stream.of(values())
        .collect(Collectors.toMap(OpenseaEventType::toString, Function.identity()));

    public static OpenseaEventType lookupInsensitive(final String name) {
        String upper = name.toUpperCase();
        return NAMES.get(upper);
    }
}
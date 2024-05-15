package ai.utiliti.bes.util;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class EventDetail {
    private final String eventName;
    private final EventParameter[] eventParameters;
}
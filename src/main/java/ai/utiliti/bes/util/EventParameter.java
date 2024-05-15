package ai.utiliti.bes.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
@Setter
public class EventParameter implements Serializable {
    private final String name;
    private final String type;
    private final String value;
}
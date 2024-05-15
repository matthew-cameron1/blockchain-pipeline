package ai.utiliti.bes.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import ai.utiliti.bes.BlockStatus;
import ai.utiliti.bes.webhooks.ContractEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class EventJob implements Serializable {

    @SerializedName("event")
    private final BlockchainEvent event;

    @SerializedName("contractEvent")
    private final ContractEvent contractEvent;

    @SerializedName("status")
    private final BlockStatus status;
}
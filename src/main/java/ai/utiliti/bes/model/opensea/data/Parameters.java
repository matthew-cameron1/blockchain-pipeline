package ai.utiliti.bes.model.opensea.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class Parameters {
    private String conduitKey;
    //private Consideration[] consideration;

    private String counter;
    private Integer endTime;
    private Offer[] offer;

    private String offerer;
    private Integer orderType;
    private String salt;
    private BigInteger startTime;
    private Integer totalOriginalConsiderationItems;
    private String zone;
    private String zoneHash;
}
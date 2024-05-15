package ai.utiliti.bes.model.opensea.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class Offer {
  private BigInteger endAmount;
  private BigInteger identifierOrCriteria;
  private Integer itemType;
  
  private BigInteger startAmount;
  private String token;
}
package ai.utiliti.bes.model.opensea.data;

import lombok.Data;

import java.util.Date;

@Data
public class Transaction {
    private String hash;
    private Date timestamp;
}
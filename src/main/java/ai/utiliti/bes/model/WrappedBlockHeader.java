package ai.utiliti.bes.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;
import org.web3j.protocol.core.methods.response.EthBlock;

import ai.utiliti.bes.model.converters.BigIntHexConverter;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="\"WrappedBlock\"", indexes = {
    @Index(columnList = "number DESC, network")
})
@Getter
@Setter
public class WrappedBlockHeader implements Serializable, Persistable<BigInteger> {

    @Id
    private BigInteger number;

    @Column(length = 65536)
    private String hash;

    @Column(length = 65536, name = "\"parentHash\"")
    private String parentHash;

    @Column(length = 65536, name = "\"sha3Uncles\"")
    private String sha3Uncles;

    @Column(length = 65536, name = "\"logsBloom\"")
    private String logsBloom;

    @Column(length = 65536, name = "\"transactionsRoot\"")
    private String transactionsRoot;

    @Column(length = 65536, name = "\"stateRoot\"")
    private String stateRoot;

    @Column(length = 65536, name = "\"receiptsRoot\"")
    private String receiptRoot;

    @Column(length = 65536)
    private String miner;
    private int network;

    private BigInteger difficulty;

    @Column(length = 65536, name = "\"extraData\"")
    private String extraData;

    @Column(name = "\"gasLimit\"")
    private BigInteger gasLimit;

    @Column(name = "\"gasUsed\"")
    private BigInteger gasUsed;
    
    private BigInteger timestamp;

    @Column(name = "\"baseFeePerGas\"", columnDefinition = "Decimal(10, 2)", precision = 10, scale = 2)
    private BigDecimal baseFeePerGas;

    @Convert(converter = BigIntHexConverter.class)
    private BigInteger nonce;

    @Column(length = 65536, name = "\"mixHash\"")
    private String mixHash;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "\"createdAt\"", updatable = false)
    private Date createdAt;

    public WrappedBlockHeader() {}

    public WrappedBlockHeader(EthBlock.Block block) {
        this.number = block.getNumber();
        this.hash = block.getHash();
        this.parentHash = block.getParentHash();
        this.sha3Uncles = block.getSha3Uncles();
        this.logsBloom = block.getLogsBloom();
        this.transactionsRoot = block.getTransactionsRoot();
        this.stateRoot = block.getStateRoot();
        this.receiptRoot = block.getReceiptsRoot();
        this.miner = block.getMiner();
        this.network = Integer.parseInt(System.getenv("NETWORK_ID"));
        this.difficulty = block.getDifficulty();
        this.extraData = block.getExtraData();
        this.gasLimit = block.getGasLimit();
        this.gasUsed = block.getGasUsed();
        this.timestamp = block.getTimestamp();
        this.baseFeePerGas = new BigDecimal(block.getBaseFeePerGas());
        this.nonce = block.getNonce();
        this.mixHash = block.getMixHash();
    }

    @Override
    public BigInteger getId() {
        return this.number;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
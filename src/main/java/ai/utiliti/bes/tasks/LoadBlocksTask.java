package ai.utiliti.bes.tasks;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoadBlocksTask implements Callable<List<EthBlock>> {

    private final int taskId;
    private final int start;
    private final int end;

    private final Web3j web3j;

    private final Logger logger = LoggerFactory.getLogger(LoadBlocksTask.class);

    @Override
    public List<EthBlock> call() {
        List<EthBlock> blocks = new ArrayList<>();
        int blocksToLoad = end - start;
        for (int i = start; i < end; i++) {
            logger.info("Task {}, has loaded {} / {}", this.taskId, i - start, blocksToLoad);
            try {
                EthBlock block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(i)), false).send();

                if (block == null) {
                    throw new Exception(String.format("Could not load block %d%n", i));
                }

                blocks.add(block);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return blocks;
    }
}

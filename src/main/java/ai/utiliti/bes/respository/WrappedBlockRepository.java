package ai.utiliti.bes.respository;

import ai.utiliti.bes.model.WrappedBlockHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.stream.Stream;

public interface WrappedBlockRepository extends JpaRepository<WrappedBlockHeader, Integer> {

    Optional<WrappedBlockHeader> findByNumber(BigInteger number);
    Stream<WrappedBlockHeader> findAllByNetwork(Integer network);
    Optional<WrappedBlockHeader> findFirstByNetworkOrderByNumberDesc(int network);
}
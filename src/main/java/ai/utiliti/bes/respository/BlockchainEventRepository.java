package ai.utiliti.bes.respository;

import ai.utiliti.bes.BlockStatus;
import ai.utiliti.bes.model.BlockchainEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.web.PageableDefault;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface BlockchainEventRepository extends PagingAndSortingRepository<BlockchainEvent, UUID> {

    Page<BlockchainEvent> findAllByStatusAndBlockNumberLessThanEqual(String status, BigInteger number, Pageable pageable);
}
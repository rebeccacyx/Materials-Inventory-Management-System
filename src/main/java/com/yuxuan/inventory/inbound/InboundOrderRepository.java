package com.yuxuan.inventory.inbound;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
    List<InboundOrder> findByWarehouseId(Long warehouseId);
    List<InboundOrder> findByStatus(InboundOrderStatus status);
    List<InboundOrder> findByWarehouseIdAndStatus(Long warehouseId, InboundOrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from InboundOrder o left join fetch o.lines l left join fetch l.item where o.id = :id")
    Optional<InboundOrder> findByIdForUpdate(@Param("id") Long id);
}

package com.yuxuan.inventory.outbound;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {
    List<OutboundOrder> findByWarehouseId(Long warehouseId);
    List<OutboundOrder> findByStatus(OutboundOrderStatus status);
    List<OutboundOrder> findByWarehouseIdAndStatus(Long warehouseId, OutboundOrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OutboundOrder o left join fetch o.lines l left join fetch l.item where o.id = :id")
    Optional<OutboundOrder> findByIdForUpdate(@Param("id") Long id);
}

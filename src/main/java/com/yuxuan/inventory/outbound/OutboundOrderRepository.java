package com.yuxuan.inventory.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {
    List<OutboundOrder> findByWarehouseId(Long warehouseId);
    List<OutboundOrder> findByStatus(OutboundOrderStatus status);
    List<OutboundOrder> findByWarehouseIdAndStatus(Long warehouseId, OutboundOrderStatus status);
}

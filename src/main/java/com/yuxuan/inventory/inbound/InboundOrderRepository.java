package com.yuxuan.inventory.inbound;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
    List<InboundOrder> findByWarehouseId(Long warehouseId);
    List<InboundOrder> findByStatus(InboundOrderStatus status);
    List<InboundOrder> findByWarehouseIdAndStatus(Long warehouseId, InboundOrderStatus status);
}

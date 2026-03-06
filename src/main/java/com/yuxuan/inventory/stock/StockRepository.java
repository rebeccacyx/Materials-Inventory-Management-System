package com.yuxuan.inventory.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
    List<Stock> findByWarehouseId(Long warehouseId);
    List<Stock> findByItemId(Long itemId);
}

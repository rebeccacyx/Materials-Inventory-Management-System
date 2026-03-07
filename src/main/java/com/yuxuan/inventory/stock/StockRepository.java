package com.yuxuan.inventory.stock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
    List<Stock> findByWarehouseId(Long warehouseId);
    List<Stock> findByItemId(Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.warehouse.id = :warehouseId and s.item.id = :itemId")
    Optional<Stock> findByWarehouseIdAndItemIdForUpdate(@Param("warehouseId") Long warehouseId,
                                                         @Param("itemId") Long itemId);
}

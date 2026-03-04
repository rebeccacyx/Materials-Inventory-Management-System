package com.yuxuan.inventory.stock;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockRepository stockRepo;

    public StockController(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    @GetMapping("/{warehouseId}/{itemId}")
    public Stock getOne(@PathVariable Long warehouseId, @PathVariable Long itemId) {
        return stockRepo.findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock not found: warehouseId=" + warehouseId + ", itemId=" + itemId
                ));
    }
}
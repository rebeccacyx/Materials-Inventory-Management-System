package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.stockMovement.dto.CreateStockMovementRequest;
import org.springframework.stereotype.Service;

@Service
public class StockMovementService {

    private final com.yuxuan.inventory.stock.StockService stockService;

    public StockMovementService(com.yuxuan.inventory.stock.StockService stockService) {
        this.stockService = stockService;
    }

    public void applyMovement(CreateStockMovementRequest request) {
        stockService.applyMovement(
                request.warehouseId(),
                request.itemId(),
                request.type(),
                request.quantity(),
                request.delta(),
                request.reason()
        );
    }
}

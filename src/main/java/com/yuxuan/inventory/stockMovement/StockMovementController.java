package com.yuxuan.inventory.stockMovement;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockService stockService;

    public StockMovementController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public String create(@RequestBody CreateStockMovementRequest request) {
        stockService.applyMovement(request);
        return "Stock updated successfully";
    }
}
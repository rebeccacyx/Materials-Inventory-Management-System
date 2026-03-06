package com.yuxuan.inventory.stock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<Stock> list(@RequestParam(required = false) Long warehouseId,
                            @RequestParam(required = false) Long itemId) {
        return stockService.query(warehouseId, itemId);
    }

    @GetMapping("/{warehouseId}/{itemId}")
    public Stock getOne(@PathVariable Long warehouseId, @PathVariable Long itemId) {
        return stockService.getByWarehouseAndItem(warehouseId, itemId);
    }
}

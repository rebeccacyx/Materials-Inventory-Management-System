package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.stockMovement.dto.CreateStockMovementRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final StockMovementRepository stockMovementRepository;

    public StockMovementController(StockMovementService stockMovementService, StockMovementRepository stockMovementRepository) {
        this.stockMovementService = stockMovementService;
        this.stockMovementRepository = stockMovementRepository;
    }

    @PostMapping
    public String create(@Valid @RequestBody CreateStockMovementRequest request) {
        stockMovementService.applyMovement(request);
        return "Stock updated successfully";
    }

    @GetMapping
    public List<StockMovement> list() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }
}

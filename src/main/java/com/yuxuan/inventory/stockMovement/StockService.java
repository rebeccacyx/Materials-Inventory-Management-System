package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.stock.Stock;
import com.yuxuan.inventory.stock.StockRepository;
import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.item.ItemRepository;
import com.yuxuan.inventory.warehouse.Warehouse;
import com.yuxuan.inventory.warehouse.WarehouseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;

    public StockService(
            StockRepository stockRepository,
            StockMovementRepository movementRepository,
            WarehouseRepository warehouseRepository,
            ItemRepository itemRepository) {

        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void applyMovement(CreateStockMovementRequest request) {

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Item item = itemRepository.findById(request.itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Stock stock = stockRepository
                .findByWarehouseIdAndItemId(request.warehouseId, request.itemId)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setWarehouse(warehouse);
                    newStock.setItem(item);
                    newStock.setQuantity(0);
                    return newStock;
                });

        long delta = calculateDelta(request);

        long newQuantity = stock.getQuantity() + delta;

        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock");
        }

        stock.setQuantity(newQuantity);
        stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setWarehouse(warehouse);
        movement.setItem(item);
        movement.setType(request.type);
        movement.setDelta(delta);
        movement.setReason(request.reason);

        movementRepository.save(movement);
    }

    private long calculateDelta(CreateStockMovementRequest request) {

        if (request.type == MovementType.IN) {
            return request.quantity;
        }

        if (request.type == MovementType.OUT) {
            return -request.quantity;
        }

        return request.delta;
    }
}
package com.yuxuan.inventory.stock;

import com.yuxuan.inventory.common.ApiException;
import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.item.ItemRepository;
import com.yuxuan.inventory.stockMovement.MovementStatus;
import com.yuxuan.inventory.stockMovement.MovementType;
import com.yuxuan.inventory.stockMovement.StockMovement;
import com.yuxuan.inventory.stockMovement.StockMovementRepository;
import com.yuxuan.inventory.warehouse.Warehouse;
import com.yuxuan.inventory.warehouse.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;

    public StockService(StockRepository stockRepository,
                        StockMovementRepository stockMovementRepository,
                        WarehouseRepository warehouseRepository,
                        ItemRepository itemRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
    }

    public Stock getByWarehouseAndItem(Long warehouseId, Long itemId) {
        return stockRepository.findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stock not found"));
    }

    public List<Stock> query(Long warehouseId, Long itemId) {
        if (warehouseId != null && itemId != null) {
            return List.of(getByWarehouseAndItem(warehouseId, itemId));
        }
        if (warehouseId != null) {
            return stockRepository.findByWarehouseId(warehouseId);
        }
        if (itemId != null) {
            return stockRepository.findByItemId(itemId);
        }
        return stockRepository.findAll();
    }

    @Transactional
    public void applyMovement(Long warehouseId, Long itemId, MovementType type, Long quantity, Long delta, String reason) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));

        Stock stock = stockRepository.findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseGet(() -> {
                    Stock s = new Stock();
                    s.setWarehouse(warehouse);
                    s.setItem(item);
                    s.setQuantity(0);
                    return s;
                });

        long realDelta = resolveDelta(type, quantity, delta);
        long newQty = stock.getQuantity() + realDelta;
        if (newQty < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock not sufficient");
        }

        stock.setQuantity(newQty);
        stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setWarehouse(warehouse);
        movement.setItem(item);
        movement.setType(type);
        movement.setDelta(realDelta);
        movement.setReason((reason == null || reason.isBlank()) ? "manual" : reason);
        movement.setStatus(MovementStatus.POSTED);
        movement.setPostedAt(Instant.now());
        stockMovementRepository.save(movement);
    }

    @Transactional
    public void applyDraftMovement(StockMovement movement) {
        Long warehouseId = movement.getWarehouse().getId();
        Long itemId = movement.getItem().getId();

        Stock stock = stockRepository.findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseGet(() -> {
                    Stock s = new Stock();
                    s.setWarehouse(movement.getWarehouse());
                    s.setItem(movement.getItem());
                    s.setQuantity(0);
                    return s;
                });

        long newQty = stock.getQuantity() + movement.getDelta();
        if (newQty < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock not sufficient");
        }

        stock.setQuantity(newQty);
        stockRepository.save(stock);
    }

    public long resolveDelta(MovementType type, Long quantity, Long delta) {
        return switch (type) {
            case IN -> requirePositive(quantity, "quantity is required for IN");
            case OUT -> -requirePositive(quantity, "quantity is required for OUT");
            case ADJUST -> {
                if (delta == null || delta == 0) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "delta is required for ADJUST");
                }
                yield delta;
            }
        };
    }

    private long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }
}

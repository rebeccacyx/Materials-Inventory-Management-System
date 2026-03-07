package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.common.ApiException;
import com.yuxuan.inventory.idempotency.IdempotencyRecord;
import com.yuxuan.inventory.idempotency.IdempotencyRecordRepository;
import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.item.ItemRepository;
import com.yuxuan.inventory.operationlog.OperationLogService;
import com.yuxuan.inventory.security.UserRole;
import com.yuxuan.inventory.stock.StockService;
import com.yuxuan.inventory.stockMovement.dto.CreateStockMovementRequest;
import com.yuxuan.inventory.warehouse.Warehouse;
import com.yuxuan.inventory.warehouse.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class StockMovementService {

    private final StockService stockService;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OperationLogService operationLogService;

    public StockMovementService(StockService stockService,
                                StockMovementRepository stockMovementRepository,
                                WarehouseRepository warehouseRepository,
                                ItemRepository itemRepository,
                                IdempotencyRecordRepository idempotencyRecordRepository,
                                OperationLogService operationLogService) {
        this.stockService = stockService;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public StockMovement createDraft(CreateStockMovementRequest request, String idempotencyKey, UserRole role) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecord existing = idempotencyRecordRepository.findByRequestKey(idempotencyKey).orElse(null);
            if (existing != null) {
                operationLogService.log("CREATE_DRAFT", "STOCK_MOVEMENT", existing.getMovement().getId(), role, "HIT", "idempotency replay");
                return existing.getMovement();
            }
        }

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));

        StockMovement movement = new StockMovement();
        movement.setWarehouse(warehouse);
        movement.setItem(item);
        movement.setType(request.type());
        movement.setDelta(stockService.resolveDelta(request.type(), request.quantity(), request.delta()));
        movement.setReason((request.reason() == null || request.reason().isBlank()) ? "manual" : request.reason());
        movement.setStatus(MovementStatus.DRAFT);
        movement.setIdempotencyKey(idempotencyKey);

        StockMovement saved = stockMovementRepository.save(movement);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecord record = new IdempotencyRecord();
            record.setRequestKey(idempotencyKey);
            record.setMovement(saved);
            idempotencyRecordRepository.save(record);
        }

        operationLogService.log("CREATE_DRAFT", "STOCK_MOVEMENT", saved.getId(), role, "SUCCESS", "movement draft created");
        return saved;
    }


    public List<StockMovement> query(Long warehouseId, Long itemId, MovementStatus status, MovementType type) {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(m -> warehouseId == null || m.getWarehouse().getId().equals(warehouseId))
                .filter(m -> itemId == null || m.getItem().getId().equals(itemId))
                .filter(m -> status == null || m.getStatus() == status)
                .filter(m -> type == null || m.getType() == type)
                .toList();
    }

    @Transactional
    public StockMovement post(Long movementId, UserRole role) {
        StockMovement movement = stockMovementRepository.findById(movementId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stock movement not found"));

        if (movement.getStatus() == MovementStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled movement cannot be posted");
        }
        if (movement.getStatus() == MovementStatus.POSTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Movement already posted");
        }

        if (movement.getDelta() < 0) {
            stockService.ensureSufficientStock(
                    movement.getWarehouse().getId(),
                    movement.getItem().getId(),
                    -movement.getDelta()
            );
        }

        stockService.applyDraftMovement(movement);
        movement.setStatus(MovementStatus.POSTED);
        movement.setPostedAt(Instant.now());
        StockMovement saved = stockMovementRepository.save(movement);
        operationLogService.log("POST", "STOCK_MOVEMENT", saved.getId(), role, "SUCCESS", "movement posted");
        return saved;
    }

    @Transactional
    public StockMovement cancel(Long movementId, UserRole role) {
        StockMovement movement = stockMovementRepository.findById(movementId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Stock movement not found"));

        if (movement.getStatus() == MovementStatus.POSTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Posted movement cannot be cancelled");
        }
        if (movement.getStatus() == MovementStatus.CANCELLED) {
            return movement;
        }

        movement.setStatus(MovementStatus.CANCELLED);
        movement.setCancelledAt(Instant.now());
        StockMovement saved = stockMovementRepository.save(movement);
        operationLogService.log("CANCEL", "STOCK_MOVEMENT", saved.getId(), role, "SUCCESS", "movement cancelled");
        return saved;
    }
}

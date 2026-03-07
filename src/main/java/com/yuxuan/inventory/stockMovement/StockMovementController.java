package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.security.RoleGuardService;
import com.yuxuan.inventory.security.UserRole;
import com.yuxuan.inventory.stockMovement.dto.CreateStockMovementRequest;
import com.yuxuan.inventory.stockMovement.dto.StockMovementResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final RoleGuardService roleGuardService;

    public StockMovementController(StockMovementService stockMovementService,
                                   RoleGuardService roleGuardService) {
        this.stockMovementService = stockMovementService;
        this.roleGuardService = roleGuardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse createDraft(@Valid @RequestBody CreateStockMovementRequest request,
                                             @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                             @RequestHeader(value = "X-Role", required = false) String roleHeader,
                                             @RequestHeader(value = "X-Operator", required = false) String operator) {
        UserRole role = roleGuardService.requireStockMutationRole(roleHeader);
        return toResponse(stockMovementService.createDraft(request, idempotencyKey, role, operator));
    }

    @PostMapping("/{id}/post")
    public StockMovementResponse post(@PathVariable Long id,
                                      @RequestHeader(value = "X-Role", required = false) String roleHeader,
                                      @RequestHeader(value = "X-Operator", required = false) String operator) {
        UserRole role = roleGuardService.requireStockMutationRole(roleHeader);
        return toResponse(stockMovementService.post(id, role, operator));
    }

    @PostMapping("/{id}/cancel")
    public StockMovementResponse cancel(@PathVariable Long id,
                                        @RequestHeader(value = "X-Role", required = false) String roleHeader,
                                        @RequestHeader(value = "X-Operator", required = false) String operator) {
        UserRole role = roleGuardService.requireStockMutationRole(roleHeader);
        return toResponse(stockMovementService.cancel(id, role, operator));
    }

    @GetMapping
    public List<StockMovementResponse> list(@RequestParam(required = false) Long warehouseId,
                                            @RequestParam(required = false) Long itemId,
                                            @RequestParam(required = false) MovementStatus status,
                                            @RequestParam(required = false) MovementType type) {
        return stockMovementService.query(warehouseId, itemId, status, type)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getWarehouse().getId(),
                movement.getWarehouse().getName(),
                movement.getItem().getId(),
                movement.getItem().getSku(),
                movement.getType().name(),
                movement.getDelta(),
                movement.getReason(),
                movement.getStatus().name(),
                movement.getCreatedBy(),
                movement.getPostedBy(),
                movement.getCancelledBy(),
                movement.getIdempotencyKey(),
                movement.getCreatedAt(),
                movement.getPostedAt(),
                movement.getCancelledAt()
        );
    }
}

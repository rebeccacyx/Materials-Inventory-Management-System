package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.security.RoleGuardService;
import com.yuxuan.inventory.security.UserRole;
import com.yuxuan.inventory.stockMovement.dto.CreateStockMovementRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final StockMovementRepository stockMovementRepository;
    private final RoleGuardService roleGuardService;

    public StockMovementController(StockMovementService stockMovementService,
                                   StockMovementRepository stockMovementRepository,
                                   RoleGuardService roleGuardService) {
        this.stockMovementService = stockMovementService;
        this.stockMovementRepository = stockMovementRepository;
        this.roleGuardService = roleGuardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovement createDraft(@Valid @RequestBody CreateStockMovementRequest request,
                                     @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                     @RequestHeader(value = "X-Role", required = false) String roleHeader) {
        UserRole role = roleGuardService.requireStockMutationRole(roleHeader);
        return stockMovementService.createDraft(request, idempotencyKey, role);
    }

    @PostMapping("/{id}/post")
    public StockMovement post(@PathVariable Long id,
                              @RequestHeader(value = "X-Role", required = false) String roleHeader) {
        UserRole role = roleGuardService.requireStockMutationRole(roleHeader);
        return stockMovementService.post(id, role);
    }

    @PostMapping("/{id}/cancel")
    public StockMovement cancel(@PathVariable Long id,
                                @RequestHeader(value = "X-Role", required = false) String roleHeader) {
        UserRole role = roleGuardService.requireStockMutationRole(roleHeader);
        return stockMovementService.cancel(id, role);
    }

    @GetMapping
    public List<StockMovement> list() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }
}

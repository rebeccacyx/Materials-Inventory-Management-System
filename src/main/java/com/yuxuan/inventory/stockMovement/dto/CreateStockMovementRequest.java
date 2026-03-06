package com.yuxuan.inventory.stockMovement.dto;

import com.yuxuan.inventory.stockMovement.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateStockMovementRequest(
        @NotNull Long warehouseId,
        @NotNull Long itemId,
        @NotNull MovementType type,
        @Positive Long quantity,
        Long delta,
        String reason
) {
}

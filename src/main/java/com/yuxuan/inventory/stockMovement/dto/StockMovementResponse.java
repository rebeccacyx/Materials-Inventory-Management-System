package com.yuxuan.inventory.stockMovement.dto;

import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        Long itemId,
        String itemSku,
        String type,
        long delta,
        String reason,
        String status,
        String idempotencyKey,
        Instant createdAt,
        Instant postedAt,
        Instant cancelledAt
) {
}

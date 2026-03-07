package com.yuxuan.inventory.outbound.dto;

import java.time.Instant;
import java.util.List;

public record OutboundOrderResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        String status,
        String createdBy,
        String postedBy,
        Instant createdAt,
        Instant postedAt,
        List<OutboundOrderLineResponse> lines
) {
}

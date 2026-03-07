package com.yuxuan.inventory.inbound.dto;

import java.time.Instant;
import java.util.List;

public record InboundOrderResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        String status,
        String createdBy,
        String postedBy,
        Instant createdAt,
        Instant postedAt,
        List<InboundOrderLineResponse> lines
) {
}

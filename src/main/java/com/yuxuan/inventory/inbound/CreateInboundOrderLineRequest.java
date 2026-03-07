package com.yuxuan.inventory.inbound;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInboundOrderLineRequest(
        @NotNull Long itemId,
        @Positive Long quantity
) {
}

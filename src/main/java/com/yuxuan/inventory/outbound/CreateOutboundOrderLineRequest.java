package com.yuxuan.inventory.outbound;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOutboundOrderLineRequest(
        @NotNull Long itemId,
        @Positive Long quantity
) {
}

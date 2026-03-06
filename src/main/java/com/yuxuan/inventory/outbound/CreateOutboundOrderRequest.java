package com.yuxuan.inventory.outbound;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOutboundOrderRequest(
        @NotNull Long warehouseId,
        @NotEmpty @Valid List<CreateOutboundOrderLineRequest> lines
) {
}

package com.yuxuan.inventory.inbound;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateInboundOrderRequest(
        @NotNull Long warehouseId,
        @NotEmpty @Valid List<CreateInboundOrderLineRequest> lines
) {
}

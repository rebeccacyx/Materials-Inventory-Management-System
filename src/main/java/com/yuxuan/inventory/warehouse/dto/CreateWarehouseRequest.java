package com.yuxuan.inventory.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWarehouseRequest(
        @NotBlank String name,
        String location
) {
}

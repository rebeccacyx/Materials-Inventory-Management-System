package com.yuxuan.inventory.item.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateItemRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String unit
) {
}

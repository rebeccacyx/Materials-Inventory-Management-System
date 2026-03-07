package com.yuxuan.inventory.outbound.dto;

public record OutboundOrderLineResponse(
        Long itemId,
        String itemSku,
        String itemName,
        long quantity
) {
}

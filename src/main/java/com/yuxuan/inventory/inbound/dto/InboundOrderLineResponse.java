package com.yuxuan.inventory.inbound.dto;

public record InboundOrderLineResponse(
        Long itemId,
        String itemSku,
        String itemName,
        long quantity
) {
}

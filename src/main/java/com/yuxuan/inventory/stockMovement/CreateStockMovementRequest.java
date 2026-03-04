package com.yuxuan.inventory.stockMovement;

public class CreateStockMovementRequest {

    public Long warehouseId;
    public Long itemId;
    public MovementType type;
    public Long quantity;   // IN / OUT 用
    public Long delta;      // ADJUST 用
    public String reason;
}
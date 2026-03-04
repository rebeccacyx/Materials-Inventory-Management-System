package com.yuxuan.inventory.stockMovement;

import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.warehouse.Warehouse;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "stock_movement")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private long delta;

    @Column(nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse=warehouse;

    }

    public void setItem(Item item) {
        this.item=item;
    }

    public void setType(MovementType type) {
        this.type=type;
    }

    public void setDelta(long delta) {
        this.delta=delta;
    }

    public void setReason(String reason) {
        this.reason =reason;
    }

    // getter/setter
}
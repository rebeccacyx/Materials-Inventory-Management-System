package com.yuxuan.inventory.stock;

import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.warehouse.Warehouse;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "stock",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stock_warehouse_item",
                columnNames = {"warehouse_id", "item_id"}
        )
)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private long quantity = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    // --- getters/setters ---
    public Long getId() { return id; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public Instant getUpdatedAt() { return updatedAt; }
}


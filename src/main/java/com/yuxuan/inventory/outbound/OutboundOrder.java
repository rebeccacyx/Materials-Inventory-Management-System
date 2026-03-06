package com.yuxuan.inventory.outbound;

import com.yuxuan.inventory.warehouse.Warehouse;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "outbound_orders")
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboundOrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderLine> lines = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        if (status == null) {
            status = OutboundOrderStatus.DRAFT;
        }
    }

    public Long getId() { return id; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public OutboundOrderStatus getStatus() { return status; }
    public void setStatus(OutboundOrderStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    public List<OutboundOrderLine> getLines() { return lines; }
}

package com.yuxuan.inventory.inbound;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yuxuan.inventory.warehouse.Warehouse;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inbound_orders")
public class InboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboundOrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundOrderLine> lines = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        if (status == null) {
            status = InboundOrderStatus.DRAFT;
        }
    }

    public Long getId() { return id; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public InboundOrderStatus getStatus() { return status; }
    public void setStatus(InboundOrderStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    public List<InboundOrderLine> getLines() { return lines; }
}

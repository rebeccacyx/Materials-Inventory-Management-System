package com.yuxuan.inventory.idempotency;

import com.yuxuan.inventory.stockMovement.StockMovement;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "idempotency_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_idempotency_key", columnNames = "request_key")
})
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_key", nullable = false, length = 128)
    private String requestKey;

    @OneToOne(optional = false)
    @JoinColumn(name = "movement_id", nullable = false)
    private StockMovement movement;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public StockMovement getMovement() {
        return movement;
    }

    public void setMovement(StockMovement movement) {
        this.movement = movement;
    }
}

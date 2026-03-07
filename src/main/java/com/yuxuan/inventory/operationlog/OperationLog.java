package com.yuxuan.inventory.operationlog;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, length = 64)
    private String target;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false, length = 32)
    private String result;

    @Column(nullable = false, length = 256)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public void setAction(String action) { this.action = action; }
    public void setTarget(String target) { this.target = target; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public void setRole(String role) { this.role = role; }
    public void setResult(String result) { this.result = result; }
    public void setDetail(String detail) { this.detail = detail; }
}

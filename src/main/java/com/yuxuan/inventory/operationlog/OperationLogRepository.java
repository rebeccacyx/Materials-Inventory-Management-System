package com.yuxuan.inventory.operationlog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    long countByAction(String action);
}

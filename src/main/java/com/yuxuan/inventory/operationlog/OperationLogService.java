package com.yuxuan.inventory.operationlog;

import com.yuxuan.inventory.security.UserRole;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public void log(String action, String target, Long targetId, UserRole role, String result, String detail) {
        OperationLog log = new OperationLog();
        log.setAction(action);
        log.setTarget(target);
        log.setTargetId(targetId);
        log.setRole(role.name());
        log.setResult(result);
        log.setDetail(detail);
        operationLogRepository.save(log);
    }
}

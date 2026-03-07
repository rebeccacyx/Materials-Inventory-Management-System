package com.yuxuan.inventory.security;

import com.yuxuan.inventory.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RoleGuardService {

    public UserRole parseRoleOrDefault(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            return UserRole.VIEWER;
        }
        try {
            return UserRole.valueOf(roleHeader.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleHeader);
        }
    }

    public UserRole requireStockMutationRole(String roleHeader) {
        UserRole role = parseRoleOrDefault(roleHeader);
        if (!role.canMutateStock()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Insufficient role permission");
        }
        return role;
    }
}

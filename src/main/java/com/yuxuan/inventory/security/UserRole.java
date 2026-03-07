package com.yuxuan.inventory.security;

public enum UserRole {
    VIEWER,
    OPERATOR,
    ADMIN;

    public boolean canMutateStock() {
        return this == OPERATOR || this == ADMIN;
    }
}

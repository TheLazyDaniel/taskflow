package com.thelazydaniel.taskflow.security.permission.enums;

public enum UserPermission implements EntityPermission{
    CREATE,
    READ,
    UPDATE,
    DELETE;

    @Override
    public String getEntityType() {
        return "USER";
    }
}

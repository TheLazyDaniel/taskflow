package com.thelazydaniel.taskflow.security.permission.enums;

public enum ProjectPermission implements EntityPermission{
    CREATE,
    READ,
    UPDATE,
    DELETE;

    @Override
    public String getEntityType() {
        return "PROJECT";
    }
}

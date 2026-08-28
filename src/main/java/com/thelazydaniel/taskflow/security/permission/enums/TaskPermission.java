package com.thelazydaniel.taskflow.security.permission.enums;

public enum TaskPermission implements EntityPermission{
    CREATE,
    READ,
    UPDATE,
    DELETE,

    //Extra
    UPDATE_STATUS,
    ASSIGN;

    @Override
    public String getEntityType() {
        return "TASK";
    }
}

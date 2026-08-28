package com.thelazydaniel.taskflow.task.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    @JsonCreator
    public static TaskPriority fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;  
        }

        try {
            return TaskPriority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid priority: " + value + ". Must be LOW, MEDIUM, HIGH, or CRITICAL"
            );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
package com.thelazydaniel.taskflow.task.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    @JsonCreator
    public static TaskStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return TaskStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid Status: " + value + ". Must be TODO, IN_PROGRESS, or DONE"
            );
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }

    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case TODO -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == DONE;
            case DONE -> target == TODO;
        };
    }
}


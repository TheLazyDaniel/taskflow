package com.thelazydaniel.taskflow.dto.request;

public record UserSearchRequest(
        String search,
        String role,
        Boolean active,
        String sortBy,
        String sortDirection,
        int page,
        int size
) {
}

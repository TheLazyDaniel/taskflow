package com.thelazydaniel.taskflow.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

public record PageRequest(
        @Min(value = 0, message = "Page has to be larger than 0")
        Integer page,  // Optional - can be null

        @Min(value = 1, message = "Size has to be larger than 0.")
        @Max(value = 100, message = "Size has to be at most 100.")
        Integer size,  // Optional

        String sortBy,
        @Pattern(regexp = "(?i)ASC|DESC", message = "Direction must be ASC or DESC (case-insensitive)")
        String direction
) {
        public PageRequest {
                if (page == null) page = 0;
                if (size == null) size = 20;
                if (sortBy == null) sortBy = "id";
                if (direction == null || direction.isBlank()) direction = "ASC";
        }

        public Pageable toPageable() {
                // Convert Hibernate SortDirection to Spring's Sort.Direction
                Sort.Direction dir = "DESC".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

                Sort sort = Sort.by(dir, sortBy);
                return org.springframework.data.domain.PageRequest.of(page, size, sort);
        }
}



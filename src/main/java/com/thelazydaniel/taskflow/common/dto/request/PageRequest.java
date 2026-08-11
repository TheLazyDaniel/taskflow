package com.thelazydaniel.taskflow.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;


@Data
@NoArgsConstructor
public class PageRequest {
        @Min(value = 0, message = "Page have to be larger than 0")
        private int page;

        @Min(value = 1, message = "Size have to be larger than 0.")
        @Max(value = 100, message = "Size have to be at most 100.")
        private int size;

        private String sortBy = "id";

        private SortDirection direction = SortDirection.ASCENDING;

        public Pageable toPageable(){
            Sort sort = Sort.by(String.valueOf(direction),sortBy);
            return org.springframework.data.domain.PageRequest.of(page,size,sort);
        }
}



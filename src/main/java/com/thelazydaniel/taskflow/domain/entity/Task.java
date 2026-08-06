package com.thelazydaniel.taskflow.domain.entity;

import com.thelazydaniel.taskflow.domain.common.BaseEntity;
import com.thelazydaniel.taskflow.domain.enums.TaskPriority;
import com.thelazydaniel.taskflow.domain.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity {
    @Column(length = 200, nullable = false)
    @Size(max = 200)
    private String title;

    @Column(length = 2000)
    @Size(max = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
}

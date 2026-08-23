package com.thelazydaniel.taskflow.task.entity;

import com.thelazydaniel.taskflow.common.entity.BaseEntity;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.task.enums.TaskPriority;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;
import com.thelazydaniel.taskflow.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task extends BaseEntity {
    @Column(length = 200, nullable = false)
    @Size(max = 200)
    private String title;

    @Column(length = 2000)
    @Size(max = 2000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private User assignee;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    private User reporter;

    @Column(name = "reporter_id",nullable = false)
    private Long reporterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Column(name = "project_id",nullable = false)
    private Long projectId;

}

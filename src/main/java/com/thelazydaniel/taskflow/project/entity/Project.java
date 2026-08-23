package com.thelazydaniel.taskflow.project.entity;

import com.thelazydaniel.taskflow.common.entity.BaseEntity;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;

import com.thelazydaniel.taskflow.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
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
public class Project extends BaseEntity {

    @Column(nullable = false, length = 100)
    @Size(min = 1, max = 100)
    private String name;

    @Column(length = 500)
    @Size(max = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    private LocalDate startDate;

    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    private void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
        this.owner = null;
    }

    public boolean isProjectActive() {
        return status.equals(ProjectStatus.ACTIVE);
    }

    public boolean isProjectArchived() {
        return status.equals(ProjectStatus.ARCHIVED);
    }
}



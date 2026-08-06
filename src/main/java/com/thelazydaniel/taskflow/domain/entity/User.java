package com.thelazydaniel.taskflow.domain.entity;

import com.thelazydaniel.taskflow.domain.common.BaseEntity;
import com.thelazydaniel.taskflow.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(name = "username",
            unique = true,
            nullable = false)
    @Size(min = 3)
    private String Username;

    @Column(unique = true)
    @Email(
            message = "Invalid email format",
            regexp = "^[A-Za-z0-9+_.-]+@(.+)$"
    )
    private String email;

    @Column(nullable = false)
    private String passwordHash;
    //need hashed before store

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "owner",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private List<Project> ownedProjects;

    @OneToMany(mappedBy = "assignee",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private List<Task> workingTasks;

    @OneToMany(mappedBy = "reporter",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private List<Task> createdTasks;

    private boolean enabled = true;

    private boolean accountNonLocked = true;

    private LocalDateTime lastLoginDate;

    public void addProject(Project project){
        this.ownedProjects.add(project);
        project.setOwner(this);
    }

    public void removeProject(Project project){
        this.ownedProjects.remove(project);
        project.setOwner(null);
    }

    public void addWorkingTask(Task task){
        this.workingTasks.add(task);
        task.setAssignee(this);
    }

    public void removeWorkingTask(Task task){
        this.workingTasks.remove(task);
        task.setAssignee(null);
    }

    public void addCreatedTask(Task task){
        this.createdTasks.add(task);
        task.setAssignee(this);
    }

    public void removeCreatedTask(Task task){
        this.createdTasks.remove(task);
        task.setAssignee(null);
    }

}

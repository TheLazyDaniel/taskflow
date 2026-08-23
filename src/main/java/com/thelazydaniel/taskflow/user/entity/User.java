package com.thelazydaniel.taskflow.user.entity;

import com.thelazydaniel.taskflow.common.entity.BaseEntity;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true,
            nullable = false)
    @Size(min = 3)
    private String username;

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
    @Column(nullable = false)
    private UserRole role;

    private boolean enabled = true;

    private boolean accountNonLocked = true;

    private LocalDateTime lastLoginDate;


}

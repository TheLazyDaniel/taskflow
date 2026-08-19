package com.thelazydaniel.taskflow.user.entity;

import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.task.entity.Task;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    private User user;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setPasswordHash("encodedPassword");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        user.setOwnedProjects(new ArrayList<>());
        user.setWorkingTasks(new ArrayList<>());
        user.setCreatedTasks(new ArrayList<>());

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
    }


    //Add project
    @Test
    void addProject_ShouldAddProjectToUserAndSetOwner() {

        assertThat(user.getOwnedProjects()).isEmpty();
        assertThat(project.getOwner()).isNull();


        user.addProject(project);

        assertThat(user.getOwnedProjects())
                .hasSize(1)
                .contains(project);
        assertThat(project.getOwner()).isEqualTo(user);
    }

    @Test
    void addProject_ShouldAddMultipleProjects() {
        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Second Project");

        user.addProject(project);
        user.addProject(project2);

        assertThat(user.getOwnedProjects())
                .hasSize(2)
                .contains(project, project2);
        assertThat(project.getOwner()).isEqualTo(user);
        assertThat(project2.getOwner()).isEqualTo(user);
    }

    @Test
    void addProject_ShouldHandleDuplicateProjects() {
        // Arrange
        user.addProject(project);
        assertThat(user.getOwnedProjects()).hasSize(1);

        // Act - Adding same project again
        user.addProject(project);

        // Assert - Should still work (List allows duplicates by default)
        // Consider using Set if you want to prevent duplicates
        assertThat(user.getOwnedProjects()).hasSize(2);
        assertThat(project.getOwner()).isEqualTo(user);
    }

    // ============================================
    // ✅ TEST: Relationship Methods - removeProject
    // ============================================
    @Test
    void removeProject_ShouldRemoveProjectAndClearOwner() {
        // Arrange - First add a project
        user.addProject(project);
        assertThat(user.getOwnedProjects()).contains(project);
        assertThat(project.getOwner()).isEqualTo(user);

        // Act
        user.removeProject(project);

        // Assert
        assertThat(user.getOwnedProjects()).isEmpty();
        assertThat(project.getOwner()).isNull();
    }

    @Test
    void removeProject_ShouldHandleRemovingNonExistentProject() {
        // Arrange - Don't add project
        assertThat(user.getOwnedProjects()).isEmpty();

        // Act - Try to remove a project that doesn't exist
        user.removeProject(project);

        // Assert - Should not throw exception (just ignore)
        assertThat(user.getOwnedProjects()).isEmpty();
        assertThat(project.getOwner()).isNull();
    }

    // ============================================
    // ✅ TEST: Relationship Methods - addWorkingTask
    // ============================================
    @Test
    void addWorkingTask_ShouldAddTaskToWorkingTasksAndSetAssignee() {
        // Arrange
        assertThat(user.getWorkingTasks()).isEmpty();
        assertThat(task.getAssignee()).isNull();

        // Act
        user.addWorkingTask(task);

        // Assert
        assertThat(user.getWorkingTasks())
                .hasSize(1)
                .contains(task);
        assertThat(task.getAssignee()).isEqualTo(user);
    }

    @Test
    void addWorkingTask_ShouldAddMultipleTasks() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Second Task");

        // Act
        user.addWorkingTask(task);
        user.addWorkingTask(task2);

        // Assert
        assertThat(user.getWorkingTasks())
                .hasSize(2)
                .contains(task, task2);
        assertThat(task.getAssignee()).isEqualTo(user);
        assertThat(task2.getAssignee()).isEqualTo(user);
    }

    // ============================================
    // ✅ TEST: Relationship Methods - removeWorkingTask
    // ============================================
    @Test
    void removeWorkingTask_ShouldRemoveTaskAndClearAssignee() {
        // Arrange
        user.addWorkingTask(task);
        assertThat(user.getWorkingTasks()).contains(task);
        assertThat(task.getAssignee()).isEqualTo(user);

        // Act
        user.removeWorkingTask(task);

        // Assert
        assertThat(user.getWorkingTasks()).isEmpty();
        assertThat(task.getAssignee()).isNull();
    }

    @Test
    void removeWorkingTask_ShouldHandleRemovingNonExistentTask() {
        // Arrange
        assertThat(user.getWorkingTasks()).isEmpty();

        // Act
        user.removeWorkingTask(task);

        // Assert
        assertThat(user.getWorkingTasks()).isEmpty();
        assertThat(task.getAssignee()).isNull();
    }

    // ============================================
    // ✅ TEST: Relationship Methods - addCreatedTask
    // ============================================
    @Test
    void addCreatedTask_ShouldAddTaskToCreatedTasksAndSetReporter() {
        // Arrange
        assertThat(user.getCreatedTasks()).isEmpty();
        assertThat(task.getReporter()).isNull();

        // Act
        user.addCreatedTask(task);

        // Assert
        assertThat(user.getCreatedTasks())
                .hasSize(1)
                .contains(task);
        assertThat(task.getReporter()).isEqualTo(user);
    }

    @Test
    void addCreatedTask_ShouldAddMultipleTasks() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Second Task");

        // Act
        user.addCreatedTask(task);
        user.addCreatedTask(task2);

        // Assert
        assertThat(user.getCreatedTasks())
                .hasSize(2)
                .contains(task, task2);
        assertThat(task.getReporter()).isEqualTo(user);
        assertThat(task2.getReporter()).isEqualTo(user);
    }

    // ============================================
    // ✅ TEST: Relationship Methods - removeCreatedTask
    // ============================================
    @Test
    void removeCreatedTask_ShouldRemoveTaskAndClearReporter() {
        // Arrange
        user.addCreatedTask(task);
        assertThat(user.getCreatedTasks()).contains(task);
        assertThat(task.getReporter()).isEqualTo(user);

        // Act
        user.removeCreatedTask(task);

        // Assert
        assertThat(user.getCreatedTasks()).isEmpty();
        assertThat(task.getReporter()).isNull();
    }

    // ============================================
    // ✅ TEST: Default Values
    // ============================================
    @Test
    void newUser_ShouldHaveDefaultValues() {
        // Arrange
        User newUser = new User();

        // Assert
        assertThat(newUser.isEnabled()).isTrue();
        assertThat(newUser.isAccountNonLocked()).isTrue();
        assertThat(newUser.getLastLoginDate()).isNull();
        assertThat(newUser.getOwnedProjects()).isNull(); // Lombok @Data creates null by default
    }

    @Test
    void newUser_WithSetters_ShouldSetValuesCorrectly() {
        // Arrange
        User newUser = new User();
        LocalDateTime now = LocalDateTime.now();

        // Act
        newUser.setUsername("john");
        newUser.setEmail("john@email.com");
        newUser.setRole(UserRole.ADMIN);
        newUser.setEnabled(false);
        newUser.setAccountNonLocked(false);
        newUser.setLastLoginDate(now);

        // Assert
        assertThat(newUser.getUsername()).isEqualTo("john");
        assertThat(newUser.getEmail()).isEqualTo("john@email.com");
        assertThat(newUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(newUser.isEnabled()).isFalse();
        assertThat(newUser.isAccountNonLocked()).isFalse();
        assertThat(newUser.getLastLoginDate()).isEqualTo(now);
    }

    // ============================================
    // ✅ TEST: Relationship Consistency
    // ============================================
    @Test
    void addProject_ShouldNotAffectOtherLists() {
        // Arrange
        user.addProject(project);

        // Assert
        assertThat(user.getWorkingTasks()).isEmpty();
        assertThat(user.getCreatedTasks()).isEmpty();
        assertThat(project.getOwner()).isEqualTo(user);
    }

    @Test
    void addWorkingTask_ShouldNotAffectOtherLists() {
        // Arrange
        user.addWorkingTask(task);

        // Assert
        assertThat(user.getOwnedProjects()).isEmpty();
        assertThat(user.getCreatedTasks()).isEmpty();
        assertThat(task.getAssignee()).isEqualTo(user);
    }

    @Test
    void addCreatedTask_ShouldNotAffectOtherLists() {
        // Arrange
        user.addCreatedTask(task);

        // Assert
        assertThat(user.getOwnedProjects()).isEmpty();
        assertThat(user.getWorkingTasks()).isEmpty();
        assertThat(task.getReporter()).isEqualTo(user);
    }

    // ============================================
    // ✅ TEST: Multiple Relationship Operations
    // ============================================
    @Test
    void user_ShouldManageMultipleRelationshipsSimultaneously() {
        // Arrange
        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Second Project");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Second Task");

        // Act
        user.addProject(project);
        user.addProject(project2);
        user.addWorkingTask(task);
        user.addWorkingTask(task2);
        user.addCreatedTask(task); // Same task can be working and created

        // Assert
        assertThat(user.getOwnedProjects()).hasSize(2);
        assertThat(user.getWorkingTasks()).hasSize(2);
        assertThat(user.getCreatedTasks()).hasSize(1);

        assertThat(project.getOwner()).isEqualTo(user);
        assertThat(project2.getOwner()).isEqualTo(user);
        assertThat(task.getAssignee()).isEqualTo(user);
        assertThat(task2.getAssignee()).isEqualTo(user);
        assertThat(task.getReporter()).isEqualTo(user);
    }

    // ============================================
    // ✅ TEST: Null Safety
    // ============================================
    @Test
    void addProject_ShouldHandleNullProject() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            user.addProject(null);
        });
    }

    @Test
    void removeProject_ShouldHandleNullProject() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            user.removeProject(null);
        });
    }

    @Test
    void addWorkingTask_ShouldHandleNullTask() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            user.addWorkingTask(null);
        });
    }

    @Test
    void addCreatedTask_ShouldHandleNullTask() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            user.addCreatedTask(null);
        });
    }
}
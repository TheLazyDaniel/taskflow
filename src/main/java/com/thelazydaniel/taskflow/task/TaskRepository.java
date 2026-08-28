package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.task.entity.Task;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAllByProject_Id(Long id, Pageable pageable);

    @Override
    @NonNull
    Page<Task> findAll(@NonNull Pageable pageable);

    Page<Task> findAllByAssignee_Id(Long assigneeId, Pageable pageable);

    Page<Task> findAllByReporter_Id(Long reporterId, Pageable pageable);

    boolean existsByAssigneeId(Long assigneeId);

    @Modifying
    @Query("UPDATE Task t SET t.projectId = null WHERE t.projectId = :projectId")
    void detachFromProject(@Param("projectId") Long projectId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Task t " +
            "WHERE (t.assigneeId = :userId OR t.reporterId = :userId) " +
            "AND t.projectId IS NOT NULL")
    boolean existsByAssigneeIdOrReporterIdAndProjectIdIsNotNull(@Param("userId") Long userId);

    boolean existsByProjectIdAndStatusOrStatus(Long projectId, TaskStatus status1, TaskStatus status2);

    boolean existsByProjectIdAndStatus(Long projectId, TaskStatus status);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Task t " +
            "WHERE (t.assigneeId = :userId OR t.reporterId = :userId) " +
            "AND t.projectId = :targetProjectId")
    boolean existsByAssigneeIdOrReporterIdAndProjectId(@Param("userId") Long userId, @Param("targetProjectId") Long projectId);
}

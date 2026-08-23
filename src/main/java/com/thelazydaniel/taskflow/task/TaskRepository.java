package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.task.entity.Task;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAllByProject_Id(Long id, Pageable pageable);

    @Override
    @NonNull
    Page<Task> findAll(@NonNull Pageable pageable);

    Page<Task> findAllByAssignee_Id(Long assigneeId, Pageable pageable);

    Page<Task> findAllByReporter_Id(Long reporterId, Pageable pageable);
}

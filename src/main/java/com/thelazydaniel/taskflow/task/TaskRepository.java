package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}

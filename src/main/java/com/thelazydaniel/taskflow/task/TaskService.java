package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public void findTasksByProject(long id){
        //implement later
    }
}

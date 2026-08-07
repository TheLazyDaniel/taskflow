package com.thelazydaniel.taskflow.controller;


import com.thelazydaniel.taskflow.service.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    public UserService userService;
}

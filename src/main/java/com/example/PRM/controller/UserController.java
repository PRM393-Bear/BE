package com.example.PRM.controller;

import com.example.PRM.dto.response.UserRes;
import com.example.PRM.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/")
public class UserController {
    public UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        UserRes userRes = userService.getUserById(userId);
        return ResponseEntity.ok(userRes);
    }
}

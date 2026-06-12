package com.example.PRM.controller;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.service.UserService;
import com.example.PRM.util.AuthDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserById() {
        UUID userId = AuthDetails.getCurrentUserId(); // ✅
        UserRes userRes = userService.getUserById(userId);
        return ResponseEntity.ok(userRes);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserById(@RequestBody UserReq userReq) {
        UUID userId = AuthDetails.getCurrentUserId(); // ✅
        userService.updateUserById(userId, userReq);
        return ResponseEntity.ok("Update user success");
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUserById() {
        UUID userId = AuthDetails.getCurrentUserId(); // ✅
        userService.deleteUserById(userId);
        return ResponseEntity.ok("Delete user success");
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            @RequestParam String confirmPassword) {
        UUID userId = AuthDetails.getCurrentUserId(); // ✅
        userService.updatePassword(userId, oldPassword, newPassword, confirmPassword);
        return ResponseEntity.ok("Update password success");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}

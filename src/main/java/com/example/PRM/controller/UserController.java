package com.example.PRM.controller;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        UserRes userRes = userService.getUserById(userId);
        return ResponseEntity.ok(userRes);
    }

    @PutMapping("{userId}")
    public ResponseEntity<?> updateUserById(@PathVariable UUID userId, @RequestBody UserReq userReq) {
        userService.updateUserById(userId, userReq);
        return ResponseEntity.ok("Update user success");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok("Delete user success");
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable UUID userId,
                                            @RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            @RequestParam String confirmPassword) {
        userService.updatePassword(userId, oldPassword, newPassword, confirmPassword);
        return ResponseEntity.ok("Update password success");
    }

}

package com.example.PRM.controller;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.service.UserService;
import com.example.PRM.status_enum.OtpPurpose;
import com.example.PRM.util.AuthDetails;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
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
                                            @RequestParam String confirmPassword,
                                            HttpServletRequest httpRequest) {
        UUID userId = AuthDetails.getCurrentUserId(); // ✅
        userService.updatePassword(userId, oldPassword, newPassword, confirmPassword, httpRequest);
        return ResponseEntity.ok("Update password success");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email,
                                     @RequestParam OtpPurpose otpPurpose){
        userService.sendOtp(email, otpPurpose);
        return ResponseEntity.ok("OTP đã được gửi đến email " + email);
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email,
                                       @RequestParam String otp,
                                       @RequestParam OtpPurpose otpPurpose) {

        String resetToken = userService.verifyOtp(email, otp, otpPurpose);

        switch (otpPurpose) {
            case REGISTER -> {
                return ResponseEntity.ok("Verify success, please login!!");
            }

            case FORGOT_PASSWORD -> {
                return ResponseEntity.ok(resetToken);
            }
        }
        return ResponseEntity.badRequest().body("Invalid OTP");
    }

    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String resetToken,
                                           @RequestParam String newPassword,
                                           @RequestParam String confirmPassword,
                                           HttpServletRequest httpRequest) {
        userService.resetPassword(resetToken, newPassword, confirmPassword, httpRequest);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
    }
}

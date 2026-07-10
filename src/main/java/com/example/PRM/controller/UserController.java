package com.example.PRM.controller;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.service.AuditLogService;
import com.example.PRM.service.UserService;
import com.example.PRM.status_enum.OtpPurpose;
import com.example.PRM.util.AuthDetails;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
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
                                            HttpServletRequest request) {
        UUID userId = AuthDetails.getCurrentUserId(); // ✅
        UserLogRes res = userService.updatePassword(userId, oldPassword, newPassword, confirmPassword);
        auditLogService.log("UPDATE_PASSWORD",
                "UPDATE_PASSWORD",
                null,
                "User update password successfully",
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );
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
                                           HttpServletRequest request) {
        UserLogRes res = userService.resetPassword(resetToken, newPassword, confirmPassword);
        auditLogService.log("RESET_PASSWORD",
                "RESET_PASSWORD",
                null,
                "User reset password successfully",
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );
        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
    }
    // ─────────────────────────────────────────
// Lấy danh sách user theo role (hiển thị bảng)
// ─────────────────────────────────────────
    @GetMapping("/by-role")
    public ResponseEntity<List<UserAdminRes>> getUsersByRole(
            @RequestParam String role
    ) {
        return ResponseEntity.ok(userService.getAllUsersByRole(role));
    }

    // ─────────────────────────────────────────
// Lấy danh sách user theo trạng thái (hiển thị bảng)
// ─────────────────────────────────────────
    @GetMapping("/by-status")
    public ResponseEntity<List<UserAdminRes>> getUsersByStatus(
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(userService.getAllUserByActive(active));
    }

    // ─────────────────────────────────────────
// Thống kê user theo role (dùng cho chart)
// ─────────────────────────────────────────
    @GetMapping("/chart/by-role")
    public ResponseEntity<Map<String, Long>> getUserChartByRole() {
        return ResponseEntity.ok(userService.getUserCountByRole());
    }

    // ─────────────────────────────────────────
// Thống kê user theo trạng thái (dùng cho chart)
// ─────────────────────────────────────────
    @GetMapping("/chart/by-status")
    public ResponseEntity<Map<String, Long>> getUserChartByStatus() {
        return ResponseEntity.ok(userService.getUserCountByStatus());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list-banned")
    public ResponseEntity<?> getListUserBannedOrUnBanned(@RequestParam boolean isBanned){
        return ResponseEntity.ok(userService.getAllUserByIsBannedAndUnbanned(isBanned));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/banned")
    public ResponseEntity<?> bannedAndUnBanned(@RequestParam UUID userId, @RequestParam boolean isBanned){
        userService.banAndUnbanUser(userId, isBanned);
        if (isBanned) {
            return ResponseEntity.ok("Banned user success");
        }
        return ResponseEntity.ok("Unbanned user success");
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody UserReq userReq){
        userService.createStaff(userReq);
        return ResponseEntity.ok("Create staff success");
    }
}

package com.example.PRM.service;

import com.example.PRM.dto.request.user.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.status_enum.OtpPurpose;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {
    public UserRes getUserById(UUID userId);
    public void deleteUserById(UUID userId);
    public void updateUserById(UUID userId, UserReq userReq);
    public UserLogRes updatePassword(UUID userId, String oldPassword, String newPassword, String confirmPasswordProductRes);
    public UserRes getUserByUsername(String username);
    // -------------- EXPORT --------------//
    public List<UserAdminRes> getAllUsers();
    public List<UserAdminRes> getAllUsersByRole(String role);
    public List<UserAdminRes> getAllUserByActive(boolean active);
    Map<String, Long> getUserCountByRole();
    Map<String, Long> getUserCountByStatus();
    //--------------------------------------//
    public String verifyOtp(String email, String otp, OtpPurpose otpPurpose);

    public UserLogRes resetPassword(String resetToken, String newPassword, String confirmPasswordProductRes);

    public void banAndUnbanUser(UUID userId, boolean active);

    public List<UserRes> getAllUserByIsBannedAndUnbanned(boolean isBanned);

    public void createStaff(UserReq userReq);



}

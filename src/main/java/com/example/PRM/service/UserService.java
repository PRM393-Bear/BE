package com.example.PRM.service;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.UUID;

public interface UserService {
    public UserRes getUserById(UUID userId);
    public void deleteUserById(UUID userId);
    public void updateUserById(UUID userId, UserReq userReq);
    public void updatePassword(UUID userId, String oldPassword, String newPassword, String confirmPassword);
    public UserRes getUserByUsername(String username);
    public List<UserAdminRes> getAllUsers();
    public void sendOtp(String email);
    public String verifyOtp(String email, String otp);
    public void resetPassword(String resetToken, String newPassword, String confirmPassword);


}

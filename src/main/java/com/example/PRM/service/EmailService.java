package com.example.PRM.service;

import com.example.PRM.status_enum.OtpPurpose;

public interface EmailService {
    public void sendOtp(String email, OtpPurpose otpPurpose);
    public void sendBannedEmail(String email, String reason);
    public void sendUnbannedEmail(String email);
}

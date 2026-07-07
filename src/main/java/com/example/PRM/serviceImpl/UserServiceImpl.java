package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.UserMapper;
import com.example.PRM.status_enum.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX   = "otp:";
    private static final String TOKEN_PREFIX = "resetToken:";
    private final AuditLogServiceImpl auditLogService;



    @Override
    public UserRes getUserById(UUID userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));

        return userMapper.getInfo(user);
    }

    @Override
    public void deleteUserById(UUID userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public void updateUserById(UUID userId, UserReq userReq) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));
        if(userReq.getFullName() != null && !userReq.getFullName().isBlank()) {
            user.setFullName(userReq.getFullName());
        }
        if(userReq.getEmail() != null && !userReq.getEmail().isBlank()) {
            user.setEmail(userReq.getEmail());
        }

        if(userReq.getPhone() != null && !userReq.getPhone().isBlank()) {
            user.setPhone(userReq.getPhone());
        }

        if(userReq.getUsername() != null && !userReq.getUsername().isBlank()) {
            user.setUserName(userReq.getUsername());
        }

        userRepository.save(user);
    }

    @Override
    public void updatePassword(UUID userId, String oldPassword, String newPassword, String confirmPassword, HttpServletRequest request) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        if(!newPassword.equals(confirmPassword)) {
            throw new BadCredentialsException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log("UPDATE_PASSWORD",
                "UPDATE_PASSWORD",
                null,
                "User update password successfully",
                "SUCCESS",
                user.getUserId(),
                user.getUserName(),
                request
        );
    }

    @Override
    public UserRes getUserByUsername(String username) {
        User user = userRepository.findByUserName(username).orElseThrow(()
                -> new NotFoundException("User not found with username: " + username));
        return userMapper.getInfo(user);
    }

    @Override
    public List<UserAdminRes> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapToUserAdminRes)
                .collect(Collectors.toList());
    }

    @Override
    public void sendOtp(String email, OtpPurpose otpPurpose) {

        String otp = String.format("%06d",
                ThreadLocalRandom.current().nextInt(100000, 1000000));

        redisTemplate.opsForValue().set(
                OTP_PREFIX + otpPurpose + ":" + email,
                otp,
                5,
                TimeUnit.MINUTES
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nguyenminhnguyen08112004@gmail.com");
            helper.setTo(email);

            String subject;
            String htmlTitle;
            String htmlDescription;

            switch (otpPurpose) {
                case FORGOT_PASSWORD -> {
                    subject      = "ECO - Mã OTP đặt lại mật khẩu";
                    htmlTitle    = "Password Reset Verification";
                    htmlDescription = "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. "
                            + "Vui lòng sử dụng mã OTP bên dưới để tiếp tục:";
                }
                case REGISTER -> {
                    subject      = "ECO - Mã OTP đăng ký tài khoản";
                    htmlTitle    = "Account Registration Verification";
                    htmlDescription = "Chúng tôi nhận được yêu cầu đăng ký tài khoản của bạn. "
                            + "Vui lòng sử dụng mã OTP bên dưới để hoàn tất đăng ký:";
                }
                default -> {
                    subject      = "ECO - Mã OTP xác thực";
                    htmlTitle    = "Verification";
                    htmlDescription = "Vui lòng sử dụng mã OTP bên dưới để tiếp tục:";
                }
            }

            helper.setSubject(subject);

            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="
                margin:0;
                padding:0;
                background:#f4f6f8;
                font-family:Arial,sans-serif;
            ">

            <div style="
                max-width:600px;
                margin:40px auto;
                background:white;
                border-radius:12px;
                overflow:hidden;
                box-shadow:0 2px 10px rgba(0,0,0,0.1);
            ">

                <div style="
                    background:#2e7d32;
                    color:white;
                    text-align:center;
                    padding:24px;
                ">
                    <h1 style="margin:0;">ECO</h1>
                    <p style="margin-top:8px;">
                        %s
                    </p>
                </div>

                <div style="padding:32px;">

                    <h2 style="color:#333;">
                        Xin chào,
                    </h2>

                    <p style="
                        color:#555;
                        line-height:1.6;
                    ">
                        %s
                    </p>

                    <div style="
                        margin:30px 0;
                        text-align:center;
                    ">
                        <div style="
                            display:inline-block;
                            background:#e8f5e9;
                            color:#2e7d32;
                            font-size:36px;
                            font-weight:bold;
                            letter-spacing:10px;
                            padding:20px 40px;
                            border-radius:10px;
                            border:2px dashed #2e7d32;
                        ">
                            %s
                        </div>
                    </div>

                    <p style="
                        color:#d32f2f;
                        font-weight:bold;
                    ">
                        Mã OTP sẽ hết hạn sau 5 phút.
                    </p>

                    <p style="
                        color:#555;
                        line-height:1.6;
                    ">
                        Nếu bạn không thực hiện yêu cầu này,
                        vui lòng bỏ qua email này.
                    </p>

                </div>

                <div style="
                    background:#f8f9fa;
                    padding:20px;
                    text-align:center;
                    color:#888;
                    font-size:12px;
                ">
                    © 2026 ECO. All rights reserved.
                </div>

            </div>

            </body>
            </html>
            """.formatted(htmlTitle, htmlDescription, otp);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP", e);
        }
    }
    @Override
    public String verifyOtp(
            String email,
            String otp,
            OtpPurpose purpose) {

        String key = OTP_PREFIX + purpose + ":" + email;

        System.out.println("Find key = " + key);

        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            throw new BadRequestException("OTP đã hết hạn");
        }

        if (!savedOtp.equals(otp)) {
            throw new BadRequestException("OTP không chính xác");
        }

        redisTemplate.delete(key);

        switch (purpose) {

            case REGISTER -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new NotFoundException("User not found"));

                user.setVerified(true);
                userRepository.save(user);

                return null;
            }

            case FORGOT_PASSWORD -> {
                String resetToken = UUID.randomUUID().toString();

                redisTemplate.opsForValue().set(
                        "resetToken:" + resetToken,
                        email,
                        10,
                        TimeUnit.MINUTES
                );

                return resetToken;
            }

            default -> {
                return null;
            }
        }
    }

    @Override
    public void resetPassword(String resetToken, String newPassword, String confirmPassword, HttpServletRequest request) {
        String email = redisTemplate.opsForValue().get(TOKEN_PREFIX + resetToken);
        System.out.println("Email = " + email);
        if (email == null) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log("RESET_PASSWORD",
                "RESET_PASSWORD",
                null,
                "User reset password successfully",
                "SUCCESS",
                user.getUserId(),
                user.getUserName(),
                request
        );

        redisTemplate.delete(TOKEN_PREFIX + resetToken);
    }
}

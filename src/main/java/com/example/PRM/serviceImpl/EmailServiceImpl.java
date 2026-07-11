package com.example.PRM.serviceImpl;

import com.example.PRM.service.EmailService;
import com.example.PRM.status_enum.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX   = "otp:";
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
}

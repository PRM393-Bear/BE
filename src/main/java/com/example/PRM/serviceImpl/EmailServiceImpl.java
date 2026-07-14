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

    @Override
    public void sendBannedEmail(String email, String reason) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nguyenminhnguyen08112004@gmail.com");
            helper.setTo(email);

            String subject = "ECO - Thông báo khóa tài khoản";

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
                background:#c62828;
                color:white;
                text-align:center;
                padding:24px;
            ">
                <h1 style="margin:0;">ECO</h1>
                <p style="margin-top:8px;">
                    Account Suspension Notice
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
                    Tài khoản của bạn đã bị khóa do vi phạm điều khoản sử dụng của chúng tôi.
                    Dưới đây là lý do cụ thể:
                </p>

                <div style="
                    margin:30px 0;
                    text-align:center;
                ">
                    <div style="
                        display:inline-block;
                        background:#ffebee;
                        color:#c62828;
                        font-size:16px;
                        font-weight:bold;
                        padding:20px 30px;
                        border-radius:10px;
                        border:2px dashed #c62828;
                        max-width:480px;
                        word-wrap:break-word;
                    ">
                        %s
                    </div>
                </div>

                <p style="
                    color:#555;
                    line-height:1.6;
                ">
                    Nếu bạn cho rằng đây là sự nhầm lẫn, vui lòng liên hệ với bộ phận
                    hỗ trợ của chúng tôi để được xem xét lại.
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
        """.formatted(reason);

            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email thông báo khóa tài khoản", e);
        }
    }
    @Override
    public void sendUnbannedEmail(String email) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nguyenminhnguyen08112004@gmail.com");
            helper.setTo(email);

            String subject = "ECO - Thông báo mở khóa tài khoản";

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
                    Account Restored
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
                    Tài khoản của bạn đã được mở khóa thành công. Bạn có thể
                    đăng nhập và tiếp tục sử dụng các dịch vụ của chúng tôi
                    như bình thường.
                </p>

                <div style="
                    margin:30px 0;
                    text-align:center;
                ">
                    <div style="
                        display:inline-block;
                        background:#e8f5e9;
                        color:#2e7d32;
                        font-size:18px;
                        font-weight:bold;
                        padding:16px 32px;
                        border-radius:10px;
                        border:2px dashed #2e7d32;
                    ">
                        Tài khoản đã được kích hoạt lại
                    </div>
                </div>

                <p style="
                    color:#555;
                    line-height:1.6;
                ">
                    Vui lòng tuân thủ điều khoản sử dụng để tránh bị khóa
                    tài khoản trong tương lai. Nếu bạn có bất kỳ thắc mắc nào,
                    hãy liên hệ với bộ phận hỗ trợ của chúng tôi.
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
        """;

            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email thông báo mở khóa tài khoản", e);
        }
    }
}

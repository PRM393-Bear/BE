package com.example.PRM.serviceImpl;

import com.example.PRM.entity.DonationRequest;
import com.example.PRM.entity.User;
import com.example.PRM.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationAdminServiceImpl {

    @Value("${app.mail.from}")
    private String senderEmail;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;



    public void notifyAdminPendingOverdue(List<DonationRequest> list) {

        if (list == null || list.isEmpty()) return;

        List<User> admins = userRepository.findByRole_RoleName("ADMIN");

        for (User admin : admins) {
            sendEmail(admin.getEmail(), list);
        }
    }

    private void sendEmail(String email, List<DonationRequest> list) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            System.out.println("Sending mail to: " + email);
            helper.setTo(email);
            helper.setSubject("ECO - Donation PENDING quá 5 ngày");

            String html = buildHtml(list);

            helper.setText(html, true);

            System.out.println("FROM = " + senderEmail);
            System.out.println("TO = " + email);

            mailSender.send(message);
            System.out.println("Mail sent successfully");

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildHtml(List<DonationRequest> list) {

        StringBuilder rows = new StringBuilder();
        for (DonationRequest d : list) {
            rows.append("""
            <tr>
                <td style="padding:12px; border-bottom:1px solid #eee; color:#333;">%s</td>
                <td style="padding:12px; border-bottom:1px solid #eee; color:#333;">%s</td>
                <td style="padding:12px; border-bottom:1px solid #eee; color:#333;">%s</td>
                <td style="padding:12px; border-bottom:1px solid #eee; color:#333;">%s</td>
                <td style="padding:12px; border-bottom:1px solid #eee;">
                    <span style="
                        background:#fff3e0;
                        color:#e65100;
                        padding:4px 10px;
                        border-radius:20px;
                        font-size:12px;
                        font-weight:bold;
                    ">%s</span>
                </td>
            </tr>
            """.formatted(
                    d.getId(),
                    d.getUser().getUserName(),
                    d.getOrganizationDetail().getOrgName(),
                    d.getCreatedAt(),
                    d.getStatus()
            ));
        }

        return """
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
            max-width:700px;
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
                    Donation Pending Alert
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
                    Hệ thống phát hiện các yêu cầu quyên góp (donation) đang ở trạng thái
                    <b>PENDING</b> quá <b>5 ngày</b>. Vui lòng kiểm tra và xử lý sớm:
                </p>

                <div style="
                    margin:30px 0;
                    overflow-x:auto;
                ">
                    <table style="
                        width:100%%;
                        border-collapse:collapse;
                        border-radius:8px;
                        overflow:hidden;
                    ">
                        <thead>
                            <tr style="background:#e8f5e9;">
                                <th style="padding:12px; text-align:left; color:#2e7d32;">ID</th>
                                <th style="padding:12px; text-align:left; color:#2e7d32;">User</th>
                                <th style="padding:12px; text-align:left; color:#2e7d32;">Organization</th>
                                <th style="padding:12px; text-align:left; color:#2e7d32;">Created At</th>
                                <th style="padding:12px; text-align:left; color:#2e7d32;">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                </div>

                <p style="
                    color:#d32f2f;
                    font-weight:bold;
                ">
                    Vui lòng xử lý các yêu cầu trên trong thời gian sớm nhất.
                </p>

                <p style="
                    color:#555;
                    line-height:1.6;
                ">
                    Đây là email tự động, vui lòng không phản hồi trực tiếp email này.
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
        """.formatted(rows.toString());
    }
}
package com.example.PRM.serviceImpl;

import com.example.PRM.status_enum.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendOtp_ShouldSendEmailAndSaveToRedis_WhenValidRequest() throws MessagingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        emailService.sendOtp("test@example.com", OtpPurpose.REGISTER);

        verify(valueOperations, times(1)).set(
                startsWith("otp:" + OtpPurpose.REGISTER + ":test@example.com"),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendOtp_ShouldThrowException_WhenMailSenderFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> emailService.sendOtp("test@example.com", OtpPurpose.REGISTER));
    }

    @Test
    void sendBannedEmail_ShouldSendEmail_WhenValidRequest() {
        emailService.sendBannedEmail("test@example.com", "Violation of policy");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendUnbannedEmail_ShouldSendEmail_WhenValidRequest() {
        emailService.sendUnbannedEmail("test@example.com");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendApprovalEmail_ShouldSendEmail_WhenValidRequest() {
        emailService.sendApprovalEmail("test@example.com");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendRejectEmail_ShouldSendEmail_WhenValidRequest() {
        emailService.sendRejectEmail("test@example.com", "Missing documents");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }
}

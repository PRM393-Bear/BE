package com.example.PRM.EmailServiceImplTest;

import com.example.PRM.serviceImpl.EmailServiceImpl;
import com.example.PRM.status_enum.OtpPurpose;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho EmailServiceImpl.
 * <p>
 * LƯU Ý QUAN TRỌNG:
 * File nguồn EmailServiceImpl hiện có nhánh {@code default} trong switch của
 * sendOtp(), nhưng enum OtpPurpose chỉ có 2 giá trị (REGISTER, FORGOT_PASSWORD),
 * nên switch đã bao phủ hết mọi giá trị -> nhánh default là dead code, KHÔNG
 * THỂ test/cover được bằng bất kỳ cách nào. Để đạt 100% coverage thật sự,
 * hãy xoá nhánh default đó trong EmailServiceImpl trước khi chạy bộ test này.
 * <p>
 * MimeMessage được tạo bằng đối tượng thật (jakarta.mail.Session) thay vì mock,
 * vì MimeMessageHelper thao tác trực tiếp lên state của MimeMessage (setFrom,
 * setTo, setText...), mock sẽ không phản ánh đúng hành vi thật.
 * <p>
 * Để cover nhánh catch(MessagingException), KHÔNG dùng
 * {@code when(...).thenThrow(new MessagingException(...))} vì Mockito từ chối
 * stub checked exception không nằm trong "throws" clause của method gốc
 * (MockitoException: "Checked exception is invalid for this method!").
 * Thay vào đó, dùng một địa chỉ email sai định dạng (INVALID_EMAIL) khiến
 * MimeMessageHelper.setTo() tự ném MessagingException thật.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private EmailServiceImpl emailService;

    private static final String TEST_EMAIL = "user@example.com";

    // Địa chỉ email cố tình sai định dạng (có khoảng trắng) để MimeMessageHelper.setTo()
    // ném MessagingException thật, dùng cho các test cover nhánh catch(MessagingException).
    private static final String INVALID_EMAIL = "invalid email@example.com";

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, redisTemplate);
    }

    private MimeMessage realMimeMessage() {
        Session session = Session.getDefaultInstance(new Properties());
        return new MimeMessage(session);
    }

    /**
     * MimeMessageHelper được tạo với multipart=true (new MimeMessageHelper(message, true, "UTF-8")),
     * nên cấu trúc nội dung có thể là String trực tiếp, hoặc MimeMultipart lồng nhiều tầng
     * (vd: multipart/mixed chứa multipart/related chứa text/html). Hàm này đệ quy tìm tới
     * tận phần text thật sự, thay vì chỉ bóc đúng 1 tầng.
     */
    private String extractHtmlContent(MimeMessage message) throws Exception {
        String found = findTextContent(message.getContent());
        assertNotNull(found, "Không tìm thấy phần nội dung text/html trong MimeMessage");
        return found;
    }

    private String findTextContent(Object content) throws Exception {
        if (content instanceof String str) {
            return str;
        }
        if (content instanceof MimeMultipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                String found = findTextContent(multipart.getBodyPart(i).getContent());
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // ==================== sendOtp ====================

    @Test
    void sendOtp_forgotPassword_shouldSetRedisAndSendEmail() throws Exception {
        MimeMessage mimeMessage = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        emailService.sendOtp(TEST_EMAIL, OtpPurpose.FORGOT_PASSWORD);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), otpCaptor.capture(), eq(5L), eq(TimeUnit.MINUTES));

        assertEquals("otp:FORGOT_PASSWORD:" + TEST_EMAIL, keyCaptor.getValue());
        assertTrue(otpCaptor.getValue().matches("\\d{6}"));

        verify(mailSender).send(mimeMessage);
        assertEquals("ECO - Mã OTP đặt lại mật khẩu", mimeMessage.getSubject());
        assertEquals("nguyenminhnguyen08112004@gmail.com", mimeMessage.getFrom()[0].toString());
        assertEquals(TEST_EMAIL, mimeMessage.getAllRecipients()[0].toString());

        String content = extractHtmlContent(mimeMessage);
        assertTrue(content.contains("Password Reset Verification"));
        assertTrue(content.contains(otpCaptor.getValue()));
    }

    @Test
    void sendOtp_register_shouldSetRedisAndSendEmail() throws Exception {
        MimeMessage mimeMessage = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        emailService.sendOtp(TEST_EMAIL, OtpPurpose.REGISTER);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        assertEquals("otp:REGISTER:" + TEST_EMAIL, keyCaptor.getValue());

        verify(mailSender).send(mimeMessage);
        assertEquals("ECO - Mã OTP đăng ký tài khoản", mimeMessage.getSubject());

        String content = extractHtmlContent(mimeMessage);
        assertTrue(content.contains("Account Registration Verification"));
    }

    @Test
    void sendOtp_whenMessagingException_shouldThrowRuntimeException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendOtp(INVALID_EMAIL, OtpPurpose.REGISTER));

        assertEquals("Không thể gửi email OTP", ex.getMessage());
        assertInstanceOf(jakarta.mail.MessagingException.class, ex.getCause());
        verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    // ==================== sendBannedEmail ====================

    @Test
    void sendBannedEmail_shouldSendEmailWithReason() throws Exception {
        MimeMessage mimeMessage = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendBannedEmail(TEST_EMAIL, "Vi phạm điều khoản");

        verify(mailSender).send(mimeMessage);
        assertEquals("ECO - Thông báo khóa tài khoản", mimeMessage.getSubject());
        String content = extractHtmlContent(mimeMessage);
        assertTrue(content.contains("Vi phạm điều khoản"));
    }

    @Test
    void sendBannedEmail_whenMessagingException_shouldThrowRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendBannedEmail(INVALID_EMAIL, "reason"));

        assertEquals("Không thể gửi email thông báo khóa tài khoản", ex.getMessage());
        assertInstanceOf(jakarta.mail.MessagingException.class, ex.getCause());
    }

    // ==================== sendUnbannedEmail ====================

    @Test
    void sendUnbannedEmail_shouldSendEmail() throws Exception {
        MimeMessage mimeMessage = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendUnbannedEmail(TEST_EMAIL);

        verify(mailSender).send(mimeMessage);
        assertEquals("ECO - Thông báo mở khóa tài khoản", mimeMessage.getSubject());
        String content = extractHtmlContent(mimeMessage);
        assertTrue(content.contains("Tài khoản đã được kích hoạt lại"));
    }

    @Test
    void sendUnbannedEmail_whenMessagingException_shouldThrowRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendUnbannedEmail(INVALID_EMAIL));

        assertEquals("Không thể gửi email thông báo mở khóa tài khoản", ex.getMessage());
        assertInstanceOf(jakarta.mail.MessagingException.class, ex.getCause());
    }

    // ==================== sendApprovalEmail ====================

    @Test
    void sendApprovalEmail_shouldSendEmail() throws Exception {
        MimeMessage mimeMessage = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendApprovalEmail(TEST_EMAIL);

        verify(mailSender).send(mimeMessage);
        assertEquals("ECO - Tổ chức của bạn đã được phê duyệt", mimeMessage.getSubject());
        String content = extractHtmlContent(mimeMessage);
        assertTrue(content.contains("Tổ chức đã được xác minh"));
    }

    @Test
    void sendApprovalEmail_whenMessagingException_shouldThrowRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendApprovalEmail(INVALID_EMAIL));

        assertEquals("Không thể gửi email phê duyệt tổ chức", ex.getMessage());
        assertInstanceOf(jakarta.mail.MessagingException.class, ex.getCause());
    }

    // ==================== sendRejectEmail ====================

    @Test
    void sendRejectEmail_shouldSendEmailWithReason() throws Exception {
        MimeMessage mimeMessage = realMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendRejectEmail(TEST_EMAIL, "Thiếu giấy tờ pháp lý");

        verify(mailSender).send(mimeMessage);
        assertEquals("ECO - Tổ chức của bạn đã bị từ chối", mimeMessage.getSubject());
        String content = extractHtmlContent(mimeMessage);
        assertTrue(content.contains("Thiếu giấy tờ pháp lý"));
    }

    @Test
    void sendRejectEmail_whenMessagingException_shouldThrowRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendRejectEmail(INVALID_EMAIL, "reason"));

        assertEquals("Không thể gửi email từ chối tổ chức", ex.getMessage());
        assertInstanceOf(jakarta.mail.MessagingException.class, ex.getCause());
    }
}
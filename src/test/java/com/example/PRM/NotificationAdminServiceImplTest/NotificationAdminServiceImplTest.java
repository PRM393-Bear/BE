package com.example.PRM.serviceImpl;

import com.example.PRM.entity.DonationRequest;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.User;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.status_enum.DonationStatus;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAdminServiceImplTest {

    @InjectMocks
    private NotificationAdminServiceImpl notificationAdminService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MimeMessage mimeMessage;

    private User adminUser;
    private DonationRequest donationRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationAdminService, "senderEmail", "noreply@example.com");

        adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setEmail("admin@example.com");

        User donor = new User();
        donor.setUserName("donorUser");

        OrganizationDetail orgDetail = new OrganizationDetail();
        orgDetail.setOrgName("Charity Org");

        donationRequest = new DonationRequest();
        donationRequest.setId(UUID.randomUUID());
        donationRequest.setUser(donor);
        donationRequest.setOrganizationDetail(orgDetail);
        donationRequest.setCreatedAt(LocalDateTime.now().minusDays(6));
        donationRequest.setStatus(DonationStatus.PENDING);
    }

    @Test
    void notifyAdminPendingOverdue_ShouldSendEmail_WhenListIsNotEmpty() {
        List<DonationRequest> list = Arrays.asList(donationRequest);

        when(userRepository.findByRole_RoleName("ADMIN")).thenReturn(Collections.singletonList(adminUser));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationAdminService.notifyAdminPendingOverdue(list);

        verify(userRepository, times(1)).findByRole_RoleName("ADMIN");
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void notifyAdminPendingOverdue_ShouldDoNothing_WhenListIsEmpty() {
        notificationAdminService.notifyAdminPendingOverdue(Collections.emptyList());

        verify(userRepository, never()).findByRole_RoleName(anyString());
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notifyAdminPendingOverdue_ShouldDoNothing_WhenListIsNull() {
        notificationAdminService.notifyAdminPendingOverdue(null);

        verify(userRepository, never()).findByRole_RoleName(anyString());
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notifyAdminPendingOverdue_ShouldHandleException_WhenMailSenderFails() {
        List<DonationRequest> list = Arrays.asList(donationRequest);

        when(userRepository.findByRole_RoleName("ADMIN")).thenReturn(Collections.singletonList(adminUser));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(mimeMessage);

        try {
            notificationAdminService.notifyAdminPendingOverdue(list);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(mailSender, times(1)).send(mimeMessage);
    }
}

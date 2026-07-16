package com.example.PRM.user;

import com.example.PRM.dto.request.user.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.UserMapper;
import com.example.PRM.repository.RoleRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.EmailServiceImpl;
import com.example.PRM.serviceImpl.UserServiceImpl;
import com.example.PRM.status_enum.OtpPurpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 * Aims for 100% line/branch coverage of every public method.
 *
 * NOTE ON PACKAGE/FOLDER LAYOUT:
 * This class is declared in package {@code com.example.PRM.serviceImpl} to match
 * the production class {@link UserServiceImpl}. If you keep your tests grouped by
 * feature folder (e.g. "user", "wardrobeitem") rather than mirroring the exact
 * production package, make sure your build tool (Maven/Gradle) is configured to
 * still pick it up — otherwise place this file under
 * {@code src/test/java/com/example/PRM/serviceImpl/} to be safe.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailServiceImpl emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        role = new Role();
        role.setRoleName("MEMBER");

        user = new User();
        user.setUserId(userId);
        user.setUserName("john.doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedOldPassword");
        user.setRole(role);
    }

    // ---------------------------------------------------------------
    // getUserById
    // ---------------------------------------------------------------

    @Test
    void getUserById_success() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        UserRes expected = new UserRes();
        when(userMapper.getInfo(user)).thenReturn(expected);

        UserRes result = userService.getUserById(userId);

        assertSame(expected, result);
    }

    @Test
    void getUserById_notFound_throwsNotFoundException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(userId));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    // ---------------------------------------------------------------
    // deleteUserById
    // ---------------------------------------------------------------

    @Test
    void deleteUserById_success() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        userService.deleteUserById(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserById_notFound_throwsNotFoundException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUserById(userId));
        verify(userRepository, never()).delete(any());
    }

    // ---------------------------------------------------------------
    // updateUserById
    // ---------------------------------------------------------------

    @Test
    void updateUserById_allFieldsProvided_updatesAllFields() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        UserReq req = new UserReq();
        req.setFullName("New Name");
        req.setEmail("new@example.com");
        req.setPhone("0123456789");
        req.setUsername("new.username");
        req.setAddress("123 New Street");

        userService.updateUserById(userId, req);

        assertEquals("New Name", user.getFullName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("0123456789", user.getPhone());
        assertEquals("new.username", user.getUserName());
        assertEquals("123 New Street", user.getAddress());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserById_allFieldsNullOrBlank_leavesFieldsUnchanged() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        String originalUserName = user.getUserName();
        String originalEmail = user.getEmail();

        UserReq req = new UserReq();
        req.setFullName("   ");
        req.setEmail(null);
        req.setPhone("");
        req.setUsername(null);
        req.setAddress("   ");

        userService.updateUserById(userId, req);

        assertNull(user.getFullName());
        assertEquals(originalEmail, user.getEmail());
        assertNull(user.getPhone());
        assertEquals(originalUserName, user.getUserName());
        assertNull(user.getAddress());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserById_notFound_throwsNotFoundException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updateUserById(userId, new UserReq()));
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // updatePassword
    // ---------------------------------------------------------------

    @Test
    void updatePassword_success() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPassword");

        UserLogRes expected = new UserLogRes();
        when(userMapper.toUserLogRes(user)).thenReturn(expected);

        UserLogRes result = userService.updatePassword(userId, "oldPass", "newPass", "newPass");

        assertSame(expected, result);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_notFound_throwsNotFoundException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updatePassword(userId, "oldPass", "newPass", "newPass"));
    }

    @Test
    void updatePassword_oldPasswordIncorrect_throwsBadCredentialsException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "encodedOldPassword")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> userService.updatePassword(userId, "wrongOld", "newPass", "newPass"));

        assertTrue(ex.getMessage().contains("Old password is incorrect"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_confirmMismatch_throwsBadCredentialsException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> userService.updatePassword(userId, "oldPass", "newPass", "differentPass"));

        assertTrue(ex.getMessage().contains("Passwords do not match"));
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // getUserByUsername
    // ---------------------------------------------------------------

    @Test
    void getUserByUsername_success() {
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));
        UserRes expected = new UserRes();
        when(userMapper.getInfo(user)).thenReturn(expected);

        UserRes result = userService.getUserByUsername("john.doe");

        assertSame(expected, result);
    }

    @Test
    void getUserByUsername_notFound_throwsNotFoundException() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByUsername("ghost"));
    }

    // ---------------------------------------------------------------
    // getAllUsers
    // ---------------------------------------------------------------

    @Test
    void getAllUsers_returnsMappedList() {
        User user2 = new User();
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        UserAdminRes res1 = new UserAdminRes();
        UserAdminRes res2 = new UserAdminRes();
        when(userMapper.mapToUserAdminRes(user)).thenReturn(res1);
        when(userMapper.mapToUserAdminRes(user2)).thenReturn(res2);

        List<UserAdminRes> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(res1, res2)));
    }

    // ---------------------------------------------------------------
    // getAllUsersByRole
    // ---------------------------------------------------------------

    @Test
    void getAllUsersByRole_found_returnsMappedList() {
        when(userRepository.findByRole_RoleName("MEMBER")).thenReturn(List.of(user));
        UserAdminRes res = new UserAdminRes();
        when(userMapper.mapToUserAdminRes(user)).thenReturn(res);

        List<UserAdminRes> result = userService.getAllUsersByRole("MEMBER");

        assertEquals(1, result.size());
        assertSame(res, result.get(0));
    }

    @Test
    void getAllUsersByRole_emptyList_throwsNotFoundException() {
        when(userRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> userService.getAllUsersByRole("ADMIN"));
    }

    @Test
    void getAllUsersByRole_nullList_throwsNotFoundException() {
        when(userRepository.findByRole_RoleName("ADMIN")).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getAllUsersByRole("ADMIN"));
    }

    // ---------------------------------------------------------------
    // getAllUserByActive
    // ---------------------------------------------------------------

    @Test
    void getAllUserByActive_found_returnsMappedList() {
        when(userRepository.findByIsVerified(true)).thenReturn(List.of(user));
        UserAdminRes res = new UserAdminRes();
        when(userMapper.mapToUserAdminRes(user)).thenReturn(res);

        List<UserAdminRes> result = userService.getAllUserByActive(true);

        assertEquals(1, result.size());
        assertSame(res, result.get(0));
    }

    @Test
    void getAllUserByActive_emptyList_throwsNotFoundException() {
        when(userRepository.findByIsVerified(false)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> userService.getAllUserByActive(false));
    }

    @Test
    void getAllUserByActive_nullList_throwsNotFoundException() {
        when(userRepository.findByIsVerified(false)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getAllUserByActive(false));
    }

    // ---------------------------------------------------------------
    // getUserCountByRole
    // ---------------------------------------------------------------

    @Test
    void getUserCountByRole_groupsUsersByRoleName() {
        User memberUser1 = new User();
        memberUser1.setRole(role);

        User memberUser2 = new User();
        memberUser2.setRole(role);

        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");
        User adminUser = new User();
        adminUser.setRole(adminRole);

        when(userRepository.findAll()).thenReturn(List.of(memberUser1, memberUser2, adminUser));

        Map<String, Long> result = userService.getUserCountByRole();

        assertEquals(2L, result.get("MEMBER"));
        assertEquals(1L, result.get("ADMIN"));
    }

    // ---------------------------------------------------------------
    // getUserCountByStatus
    // ---------------------------------------------------------------

    @Test
    void getUserCountByStatus_returnsActiveAndInactiveCounts() {
        when(userRepository.countByVerified(true)).thenReturn(4L);
        when(userRepository.countByVerified(false)).thenReturn(6L);

        Map<String, Long> result = userService.getUserCountByStatus();

        assertEquals(4L, result.get("active"));
        assertEquals(6L, result.get("inactive"));
    }

    // ---------------------------------------------------------------
    // verifyOtp
    // ---------------------------------------------------------------

    @Test
    void verifyOtp_otpExpired_throwsBadRequestException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.verifyOtp("john@example.com", "123456", OtpPurpose.REGISTER));

        assertTrue(ex.getMessage().contains("hết hạn"));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyOtp_otpIncorrect_throwsBadRequestException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("999999");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.verifyOtp("john@example.com", "123456", OtpPurpose.REGISTER));

        assertTrue(ex.getMessage().contains("không chính xác"));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyOtp_register_success_marksUserVerified() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        String result = userService.verifyOtp("john@example.com", "123456", OtpPurpose.REGISTER);

        assertNull(result);
        assertTrue(user.getIsVerified());
        verify(userRepository).save(user);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void verifyOtp_register_userNotFound_throwsNotFoundException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.verifyOtp("ghost@example.com", "123456", OtpPurpose.REGISTER));
    }

    @Test
    void verifyOtp_forgotPassword_returnsResetToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("123456");

        String result = userService.verifyOtp("john@example.com", "123456", OtpPurpose.FORGOT_PASSWORD);

        assertNotNull(result);
        verify(valueOperations).set(
                eq("resetToken:" + result),
                eq("john@example.com"),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }

    // NOTE: OtpPurpose currently only has REGISTER and FORGOT_PASSWORD, so the
    // switch's `default` branch is unreachable in practice. javac still emits a
    // default case in the generated switch table for enum switches, which some
    // coverage tools (e.g. JaCoco) may flag as an uncovered branch even though
    // the code can never execute it. If you later add another OtpPurpose value,
    // add a test here exercising it through the default branch.

    // ---------------------------------------------------------------
    // resetPassword
    // ---------------------------------------------------------------

    @Test
    void resetPassword_success() {
        String token = "abc-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("resetToken:" + token)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPassword");

        UserLogRes expected = new UserLogRes();
        when(userMapper.toUserLogRes(user)).thenReturn(expected);

        UserLogRes result = userService.resetPassword(token, "newPass", "newPass");

        assertSame(expected, result);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
        verify(redisTemplate).delete("resetToken:" + token);
    }

    @Test
    void resetPassword_invalidToken_throwsRuntimeException() {
        String token = "bad-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("resetToken:" + token)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.resetPassword(token, "newPass", "newPass"));
        assertTrue(ex.getMessage().contains("Token không hợp lệ"));
    }

    @Test
    void resetPassword_passwordMismatch_throwsRuntimeException() {
        String token = "abc-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("resetToken:" + token)).thenReturn("john@example.com");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.resetPassword(token, "newPass", "differentPass"));
        assertTrue(ex.getMessage().contains("không khớp"));
    }

    @Test
    void resetPassword_userNotFound_throwsRuntimeException() {
        String token = "abc-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("resetToken:" + token)).thenReturn("ghost@example.com");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.resetPassword(token, "newPass", "newPass"));
        assertTrue(ex.getMessage().contains("không tồn tại"));
    }

    // ---------------------------------------------------------------
    // banAndUnbanUser
    // ---------------------------------------------------------------

    @Test
    void banAndUnbanUser_ban_sendsBannedEmail() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        userService.banAndUnbanUser(userId, true, "violation");

        assertTrue(user.getIsBlocked());
        verify(userRepository).save(user);
        verify(emailService).sendBannedEmail(user.getEmail(), "violation");
        verify(emailService, never()).sendUnbannedEmail(anyString());
    }

    @Test
    void banAndUnbanUser_unban_sendsUnbannedEmail() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        userService.banAndUnbanUser(userId, false, "resolved");

        assertFalse(user.getIsBlocked());
        verify(userRepository).save(user);
        verify(emailService).sendUnbannedEmail(user.getEmail());
        verify(emailService, never()).sendBannedEmail(anyString(), anyString());
    }

    @Test
    void banAndUnbanUser_userNotFound_throwsRuntimeException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.banAndUnbanUser(userId, true, "reason"));
        verifyNoInteractions(emailService);
    }

    @Test
    void banAndUnbanUser_roleNotAllowed_throwsBadRequestException() {
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");
        user.setRole(adminRole);
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.banAndUnbanUser(userId, true, "reason"));

        assertTrue(ex.getMessage().contains("Can not ban or unban"));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // ---------------------------------------------------------------
    // getAllUserByIsBannedAndUnbanned
    // ---------------------------------------------------------------

    @Test
    void getAllUserByIsBannedAndUnbanned_returnsMappedList() {
        when(userRepository.findByIsBlocked(true)).thenReturn(List.of(user));
        UserRes res = new UserRes();
        when(userMapper.getInfo(user)).thenReturn(res);

        List<UserRes> result = userService.getAllUserByIsBannedAndUnbanned(true);

        assertEquals(1, result.size());
        assertSame(res, result.get(0));
    }

    @Test
    void getAllUserByIsBannedAndUnbanned_emptyList_returnsEmptyList() {
        when(userRepository.findByIsBlocked(false)).thenReturn(List.of());

        List<UserRes> result = userService.getAllUserByIsBannedAndUnbanned(false);

        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------
    // createStaff
    // ---------------------------------------------------------------

    @Test
    void createStaff_success() {
        UserReq req = new UserReq();
        req.setRoleName("STAFF");
        req.setPassword("rawPassword");

        User newStaff = new User();
        when(userMapper.toEntity(req)).thenReturn(newStaff);

        Role staffRole = new Role();
        staffRole.setRoleName("STAFF");
        when(roleRepository.findByRoleName("STAFF")).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        userService.createStaff(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertSame(staffRole, saved.getRole());
        assertEquals("encodedPassword", saved.getPassword());
        assertTrue(saved.getIsVerified());
        assertFalse(saved.getIsBlocked());
    }

    @Test
    void createStaff_roleNotFound_throwsRuntimeException() {
        UserReq req = new UserReq();
        req.setRoleName("UNKNOWN");
        req.setPassword("rawPassword");

        User newStaff = new User();
        when(userMapper.toEntity(req)).thenReturn(newStaff);
        when(roleRepository.findByRoleName("UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createStaff(req));
        assertTrue(ex.getMessage().contains("Role not found"));
        verify(userRepository, never()).save(any());
    }
}
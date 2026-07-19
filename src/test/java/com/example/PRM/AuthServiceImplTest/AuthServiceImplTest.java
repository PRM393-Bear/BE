package com.example.PRM.AuthServiceImplTest;

import com.example.PRM.dto.request.user.LoginReq;
import com.example.PRM.dto.request.user.UserReq;
import com.example.PRM.dto.user.LoginLogRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.RefreshToken;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.repository.RefreshTokenRepository;
import com.example.PRM.repository.RoleRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.EmailService;
import com.example.PRM.serviceImpl.AuthServiceImpl;
import com.example.PRM.serviceImpl.RefreshTokenServiceImpl;
import com.example.PRM.serviceImpl.UserDetailsServiceImpl;
import com.example.PRM.serviceImpl.UserServiceImpl;
import com.example.PRM.status_enum.VerificationOrganizationStatus;
import com.example.PRM.status_enum.OtpPurpose;
import com.example.PRM.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OrganizationDetailRepository organizationDetailRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role memberRole;
    private Role orgRole;

    @BeforeEach
    void setup() {

        memberRole = new Role();
        memberRole.setRoleName("MEMBER");

        orgRole = new Role();
        orgRole.setRoleName("ORGANIZATION");

        user = User.builder()
                .userName("john")
                .email("john@test.com")
                .password("encoded")
                .role(memberRole)
                .isVerified(true)
                .isBlocked(false)
                .build();
    }

    // =====================================================
    // REGISTER
    // =====================================================

    @Test
    void registerForMember_UsernameExists() {

        UserReq req = new UserReq();
        req.setUsername("john");

        when(userRepository.existsByUserName("john"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> authService.registerForMember(req)
        );
    }

    @Test
    void registerForMember_EmailExists() {

        UserReq req = new UserReq();
        req.setUsername("john");
        req.setEmail("john@test.com");

        when(userRepository.existsByUserName(any()))
                .thenReturn(false);

        when(userRepository.existsByEmail(any()))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> authService.registerForMember(req)
        );
    }

    @Test
    void registerForMember_RoleNotFound() {

        UserReq req = new UserReq();
        req.setUsername("john");
        req.setEmail("john@test.com");
        req.setRoleName("member");

        when(userRepository.existsByUserName(any()))
                .thenReturn(false);

        when(userRepository.existsByEmail(any()))
                .thenReturn(false);

        when(roleRepository.findByRoleName(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> authService.registerForMember(req)
        );
    }

    @Test
    void registerForMember_Success() {

        UserReq req = new UserReq();

        req.setUsername("john");
        req.setEmail("john@test.com");
        req.setPassword("123");
        req.setRoleName("member");

        when(userRepository.existsByUserName(any()))
                .thenReturn(false);

        when(userRepository.existsByEmail(any()))
                .thenReturn(false);

        when(roleRepository.findByRoleName(any()))
                .thenReturn(Optional.of(memberRole));

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");

        UserLogRes res =
                authService.registerForMember(req);

        verify(userRepository).save(any(User.class));

        verify(emailService)
                .sendOtp("john@test.com", OtpPurpose.REGISTER);

        assertEquals("john", res.getUsername());
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @Test
    void login_AuthenticationFailed() {

        LoginReq req = new LoginReq();
        req.setUsername("john");
        req.setPassword("123");

        doThrow(new NotFoundException("bad"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(
                NotFoundException.class,
                () -> authService.login(req)
        );
    }

    @Test
    void login_UserNotFound() {

        LoginReq req = new LoginReq();
        req.setUsername("john");

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.empty());

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        assertThrows(
                NotFoundException.class,
                () -> authService.login(req)
        );
    }

    @Test
    void login_UserNotVerified() {

        user.setIsVerified(false);

        LoginReq req = new LoginReq();
        req.setUsername("john");

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadRequestException.class,
                () -> authService.login(req)
        );
    }

    @Test
    void login_UserBlocked() {

        user.setIsBlocked(true);

        LoginReq req = new LoginReq();
        req.setUsername("john");

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadRequestException.class,
                () -> authService.login(req)
        );
    }

    @Test
    void login_Success_NormalUser() {

        LoginReq req = new LoginReq();
        req.setUsername("john");

        RefreshToken token = new RefreshToken();
        token.setToken("refresh");

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(any()))
                .thenReturn("access");

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(token));

        when(roleRepository.findByRoleName("ORGANIZATION"))
                .thenReturn(Optional.of(orgRole));

        LoginLogRes res = authService.login(req);

        assertEquals("access", res.getAccessToken());
    }

    @Test
    void login_Success_CreateRefreshToken() {

        LoginReq req = new LoginReq();
        req.setUsername("john");

        RefreshToken token = new RefreshToken();
        token.setToken("refresh");

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(any()))
                .thenReturn("access");

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.empty());

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(token);

        when(roleRepository.findByRoleName("ORGANIZATION"))
                .thenReturn(Optional.of(orgRole));

        authService.login(req);

        verify(refreshTokenService)
                .createRefreshToken(user);
    }

    @Test
    void login_Success_OrganizationWithDetail() {

        user.setRole(orgRole);

        LoginReq req = new LoginReq();
        req.setUsername("john");

        RefreshToken token = new RefreshToken();
        token.setToken("refresh");

        OrganizationDetail od = new OrganizationDetail();
        od.setStatus(VerificationOrganizationStatus.APPROVED);
        od.setId(UUID.randomUUID());

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(any()))
                .thenReturn("access");

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(token));

        when(roleRepository.findByRoleName("ORGANIZATION"))
                .thenReturn(Optional.of(orgRole));

        when(organizationDetailRepository.findByUser_UserId(any()))
                .thenReturn(Optional.of(od));

        LoginLogRes res = authService.login(req);

        assertNotNull(res);
    }
    @Test
    void login_OrganizationWithoutOrganizationDetail() {

        user.setRole(orgRole);

        LoginReq req = new LoginReq();
        req.setUsername("john");

        RefreshToken token = new RefreshToken();
        token.setToken("refresh");

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(mock(UserDetails.class));

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(any()))
                .thenReturn("access");

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(token));

        when(roleRepository.findByRoleName("ORGANIZATION"))
                .thenReturn(Optional.of(orgRole));

        // NHÁNH CÒN THIẾU
        when(organizationDetailRepository.findByUser_UserId(any()))
                .thenReturn(Optional.empty());

        LoginLogRes result = authService.login(req);

        assertNotNull(result);
        assertEquals("access", result.getAccessToken());
    }
    @Test
    void login_OrganizationRoleNotFound() {

        LoginReq req = new LoginReq();
        req.setUsername("john");
        req.setPassword("123");

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(userDetails);

        when(userRepository.findByUserName(any()))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(any()))
                .thenReturn("access");

        RefreshToken token = new RefreshToken();
        token.setToken("refresh");

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(token));

        // branch còn thiếu
        when(roleRepository.findByRoleName("ORGANIZATION"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> authService.login(req)
        );
    }
}
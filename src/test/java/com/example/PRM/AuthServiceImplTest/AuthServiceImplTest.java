package com.example.PRM.serviceImpl;

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

    @InjectMocks
    private AuthServiceImpl authService;

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

    private UserReq userReq;
    private LoginReq loginReq;
    private User user;
    private Role role;
    private RefreshToken refreshToken;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setRoleName("USER");

        userReq = new UserReq();
        userReq.setUsername("testuser");
        userReq.setEmail("test@gmail.com");
        userReq.setPassword("password123");
        userReq.setRoleName("USER");
        userReq.setFullName("Test User");
        userReq.setPhone("0123456789");
        userReq.setAddress("Test Address");

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("testuser");
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(role);
        user.setIsVerified(true);
        user.setIsBlocked(false);

        loginReq = new LoginReq();
        loginReq.setUsername("testuser");
        loginReq.setPassword("password123");

        refreshToken = new RefreshToken();
        refreshToken.setToken("sample-refresh-token");

        userDetails = mock(UserDetails.class);
    }

    @Test
    void registerForMember_ShouldReturnUserLogRes_WhenValidRequest() {
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserLogRes response = authService.registerForMember(userReq);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendOtp(eq("test@gmail.com"), eq(OtpPurpose.REGISTER));
    }

    @Test
    void registerForMember_ShouldThrowBadRequest_WhenUsernameExists() {
        when(userRepository.existsByUserName(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registerForMember(userReq));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerForMember_ShouldThrowBadRequest_WhenEmailExists() {
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registerForMember(userReq));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerForMember_ShouldThrowNotFound_WhenRoleNotFound() {
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.registerForMember(userReq));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnLoginLogRes_WhenValidCredentialsAndNotOrganization() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(userDetails)).thenReturn("sample-access-token");
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(refreshToken));
        
        Role orgRole = new Role();
        orgRole.setRoleName("ORGANIZATION");
        when(roleRepository.findByRoleName("ORGANIZATION")).thenReturn(Optional.of(orgRole));

        LoginLogRes response = authService.login(loginReq);

        assertNotNull(response);
        assertEquals("sample-access-token", response.getAccessToken());
        assertEquals("sample-refresh-token", response.getRefreshToken());
        assertEquals("testuser", response.getUsername());
        assertNull(response.getOrganizationStatus());
    }

    @Test
    void login_ShouldReturnLoginLogRes_WhenOrganization() {
        Role orgRole = new Role();
        orgRole.setRoleName("ORGANIZATION");
        user.setRole(orgRole);
        
        OrganizationDetail orgDetail = new OrganizationDetail();
        orgDetail.setId(UUID.randomUUID());
        orgDetail.setStatus(VerificationOrganizationStatus.APPROVED);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(userDetails)).thenReturn("sample-access-token");
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(refreshToken));
        when(roleRepository.findByRoleName("ORGANIZATION")).thenReturn(Optional.of(orgRole));
        when(organizationDetailRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(orgDetail));

        LoginLogRes response = authService.login(loginReq);

        assertNotNull(response);
        assertEquals("sample-access-token", response.getAccessToken());
        assertEquals(VerificationOrganizationStatus.APPROVED.toString(), response.getOrganizationStatus());
        assertEquals(orgDetail.getId().toString(), response.getOrganizationId());
    }

    @Test
    void login_ShouldThrowBadRequest_WhenUserNotVerified() {
        user.setIsVerified(false);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.login(loginReq));
    }

    @Test
    void login_ShouldThrowBadRequest_WhenUserBlocked() {
        user.setIsBlocked(true);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.login(loginReq));
    }

    @Test
    void login_ShouldThrowNotFound_WhenUserNotFoundInDB() {
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.login(loginReq));
    }
}

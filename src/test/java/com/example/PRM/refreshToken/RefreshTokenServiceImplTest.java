package com.example.PRM.refreshToken;

import com.example.PRM.entity.RefreshToken;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.repository.RefreshTokenRepository;
import com.example.PRM.serviceImpl.AuditLogServiceImpl;
import com.example.PRM.serviceImpl.RefreshTokenServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RefreshTokenServiceImpl}.
 *
 * NOTE: `expirationDays` is populated by Spring via {@code @Value} at runtime and is not
 * part of the constructor, so it is set manually with ReflectionTestUtils in setUp().
 *
 * NOTE: the exact parameter types of {@code auditLogService.log(...)} aren't visible in the
 * snippet provided, so the revokeByUser test verifies the call using argument matchers
 * (eq / isNull) positionally, matching the 8 arguments in the source. If the real method
 * signature differs in arg count/types, adjust the verify(...) call accordingly.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuditLogServiceImpl auditLogService;

    @Mock
    private HttpServletRequest request;

    private RefreshTokenServiceImpl refreshTokenService;

    private User user;
    private static final long EXPIRATION_DAYS = 30L;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository, auditLogService);
        ReflectionTestUtils.setField(refreshTokenService, "expirationDays", EXPIRATION_DAYS);

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("john.doe");
    }

    // ---------------------------------------------------------------
    // createRefreshToken
    // ---------------------------------------------------------------

    @Test
    void createRefreshToken_deletesOldTokenAndSavesNewOne() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        RefreshToken result = refreshTokenService.createRefreshToken(user);
        Instant after = Instant.now();

        verify(refreshTokenRepository).deleteByUser(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertSame(result, saved);
        assertNotNull(saved.getToken());
        assertDoesNotThrow(() -> UUID.fromString(saved.getToken()));
        assertSame(user, saved.getUser());

        Instant expectedMin = before.plus(EXPIRATION_DAYS, ChronoUnit.DAYS);
        Instant expectedMax = after.plus(EXPIRATION_DAYS, ChronoUnit.DAYS);
        assertFalse(saved.getExpiresAt().isBefore(expectedMin));
        assertFalse(saved.getExpiresAt().isAfter(expectedMax));
    }

    // ---------------------------------------------------------------
    // rotate
    // ---------------------------------------------------------------

    @Test
    void rotate_tokenNotFound_throwsBadRequestException() {
        when(refreshTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> refreshTokenService.rotate("missing-token"));
        assertTrue(ex.getMessage().contains("không tồn tại"));

        verify(refreshTokenRepository, never()).delete(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotate_tokenExpired_deletesAndThrowsBadRequestException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> refreshTokenService.rotate("expired-token"));
        assertTrue(ex.getMessage().contains("hết hạn"));

        verify(refreshTokenRepository).delete(expiredToken);
        verify(refreshTokenRepository, never()).flush();
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotate_tokenValid_deletesOldAndCreatesNewToken() {
        RefreshToken validToken = RefreshToken.builder()
                .token("valid-token")
                .user(user)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.rotate("valid-token");

        verify(refreshTokenRepository).delete(validToken);
        verify(refreshTokenRepository).flush();
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));

        assertNotNull(result);
        assertSame(user, result.getUser());
        assertNotEquals("valid-token", result.getToken());
    }

    // ---------------------------------------------------------------
    // revokeByUser
    // ---------------------------------------------------------------

    @Test
    void revokeByUser_logsAndDeletesUserTokens() {
        refreshTokenService.revokeByUser(user, request);

        verify(auditLogService).log(
                eq("LOGOUT"),
                eq("LOGOUT"),
                isNull(),
                eq("User log out successfully"),
                eq("SUCCESS"),
                eq(user.getUserId()),
                eq(user.getUserName()),
                eq(request)
        );
        verify(refreshTokenRepository).deleteByUser(user);
    }
}

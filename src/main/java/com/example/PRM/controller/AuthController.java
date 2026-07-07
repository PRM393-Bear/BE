package com.example.PRM.controller;

import com.example.PRM.dto.request.LoginReq;
import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.AuthRes;
import com.example.PRM.entity.RefreshToken;
import com.example.PRM.repository.RefreshTokenRepository;
import com.example.PRM.service.RefreshTokenService;
import com.example.PRM.serviceImpl.UserDetailsServiceImpl;
import com.example.PRM.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.PRM.serviceImpl.AuthServiceImpl;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserReq request,
                                      HttpServletRequest httpRequest) {
        authService.registerForMember(request, httpRequest);
        return ResponseEntity.ok("User registered successfully, please check your email to verify your account!!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@RequestBody LoginReq request,
                                         HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    // Lấy access token mới bằng refresh token
    @PostMapping("/refresh")
    public ResponseEntity<AuthRes> refresh(@RequestParam String refreshToken) {
        RefreshToken newRefreshToken = refreshTokenService.rotate(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(
                newRefreshToken.getUser().getUserName()
        );
        String newAccessToken = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthRes(newAccessToken, newRefreshToken.getToken()));
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String refreshToken , HttpServletRequest httpRequest) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(t -> refreshTokenService.revokeByUser(t.getUser(),httpRequest));
        return ResponseEntity.ok("Đăng xuất thành công");
    }

}

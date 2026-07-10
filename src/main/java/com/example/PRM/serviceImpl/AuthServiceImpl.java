package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.LoginReq;
import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.user.AuthRes;
import com.example.PRM.dto.user.LoginLogRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.entity.RefreshToken;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.RefreshTokenRepository;
import com.example.PRM.status_enum.OtpPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.PRM.repository.RoleRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserServiceImpl userService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserLogRes registerForMember(UserReq request) {
        if (userRepository.existsByUserName(request.getUsername())) {
            throw new BadRequestException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        Role role = roleRepository.findByRoleName(request.getRoleName().toUpperCase()).orElseThrow(()
                -> new NotFoundException("Role not found with name: " + request.getRoleName()));

        User user = User.builder()
                .userName(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isVerified(false)
                .isBlocked(false)
                .build();

        userRepository.save(user);

        userService.sendOtp(user.getEmail(), OtpPurpose.REGISTER);
        return new UserLogRes(user.getUserName(),user.getUserId());
    }

    public LoginLogRes login(LoginReq request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new NotFoundException("Sai tài khoản hoặc mật khẩu");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if(!user.getIsVerified()){
            throw new BadRequestException("User must verify email before login!");
        }

        if(user.getIsBlocked()){
            throw new BadRequestException("User is blocked!");
        }
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> refreshTokenService.createRefreshToken(user));


        return new LoginLogRes(accessToken, refreshToken.getToken(),user.getUserName(),user.getUserId());
    }
}

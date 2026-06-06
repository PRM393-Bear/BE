package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.LoginReq;
import com.example.PRM.dto.request.RegisterReq;
import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.AuthRes;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.NotFoundException;
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

    public AuthRes register(UserReq request) {
        if (userRepository.existsByUserName(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Role role = roleRepository.findByRoleName("MEMBER").
                orElseThrow(() -> new RuntimeException("Role ADMIN không tồn tại"));


        User user = User.builder()
                .userName(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthRes(token, user.getUserName(), user.getRole());
    }

    public AuthRes login(LoginReq request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        User user = userRepository.findByUserName(request.getUsername()).orElseThrow();
        return new AuthRes(token, user.getUserName(), user.getRole());
    }
}

package com.example.PRM.serviceImpl;

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
import com.example.PRM.status_enum.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.UserService;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final RoleRepository roleRepository;

    private static final String OTP_PREFIX   = "otp:";
    private static final String TOKEN_PREFIX = "resetToken:";



    @Override
    public UserRes getUserById(UUID userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));

        return userMapper.getInfo(user);
    }

    @Override
    public void deleteUserById(UUID userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public void updateUserById(UUID userId, UserReq userReq) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));
        if(userReq.getFullName() != null && !userReq.getFullName().isBlank()) {
            user.setFullName(userReq.getFullName());
        }
        if(userReq.getEmail() != null && !userReq.getEmail().isBlank()) {
            user.setEmail(userReq.getEmail());
        }

        if(userReq.getPhone() != null && !userReq.getPhone().isBlank()) {
            user.setPhone(userReq.getPhone());
        }

        if(userReq.getUsername() != null && !userReq.getUsername().isBlank()) {
            user.setUserName(userReq.getUsername());
        }

        if(userReq.getAddress() != null && !userReq.getAddress().isBlank()) {
            user.setAddress(userReq.getAddress());
        }

        userRepository.save(user);
    }

    @Override
    public UserLogRes updatePassword(UUID userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        if(!newPassword.equals(confirmPassword)) {
            throw new BadCredentialsException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return userMapper.toUserLogRes(user);
    }

    @Override
    public UserRes getUserByUsername(String username) {
        User user = userRepository.findByUserName(username).orElseThrow(()
                -> new NotFoundException("User not found with username: " + username));
        return userMapper.getInfo(user);
    }

    @Override
    public List<UserAdminRes> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapToUserAdminRes)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAdminRes> getAllUsersByRole(String role) {
        List<User> users = userRepository.findByRole_RoleName(role);
        if (users != null && !users.isEmpty()) {
            return users.stream()
                    .map(userMapper::mapToUserAdminRes)
                    .collect(Collectors.toList());
        }
        throw new NotFoundException("User not found");
    }

    @Override
    public List<UserAdminRes> getAllUserByActive(boolean active) {
        List<User> users = userRepository.findByIsVerified(active);
        if (users != null && !users.isEmpty()) {
            return users.stream()
                    .map(userMapper::mapToUserAdminRes)
                    .collect(Collectors.toList());
        }
        throw new NotFoundException("User not found");
    }
    @Override
    public Map<String, Long> getUserCountByRole() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getRole().getRoleName(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getUserCountByStatus() {
        long active   = userRepository.countByVerified(true);
        long inactive = userRepository.countByVerified(false);
        return Map.of(
                "active",   active,
                "inactive", inactive
        );
    }

    @Override
    public String verifyOtp(
            String email,
            String otp,
            OtpPurpose purpose) {

        String key = OTP_PREFIX + purpose + ":" + email;

        System.out.println("Find key = " + key);

        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            throw new BadRequestException("OTP đã hết hạn");
        }

        if (!savedOtp.equals(otp)) {
            throw new BadRequestException("OTP không chính xác");
        }

        redisTemplate.delete(key);

        switch (purpose) {

            case REGISTER -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new NotFoundException("User not found"));
                    user.setIsVerified(true);
                    userRepository.save(user);

                return null;
            }

            case FORGOT_PASSWORD -> {
                String resetToken = UUID.randomUUID().toString();

                redisTemplate.opsForValue().set(
                        "resetToken:" + resetToken,
                        email,
                        10,
                        TimeUnit.MINUTES
                );

                return resetToken;
            }

            default -> {
                return null;
            }
        }
    }

    @Override
    public UserLogRes resetPassword(String resetToken, String newPassword, String confirmPassword) {
        String email = redisTemplate.opsForValue().get(TOKEN_PREFIX + resetToken);
        System.out.println("Email = " + email);
        if (email == null) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);


        redisTemplate.delete(TOKEN_PREFIX + resetToken);
        return userMapper.toUserLogRes(user);
    }

    @Override
    public void banAndUnbanUser(UUID userId, boolean active) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setIsBlocked(active);
        userRepository.save(user);
    }

    @Override
    public List<UserRes> getAllUserByIsBannedAndUnbanned(boolean isBanned) {
        List<User> users = userRepository.findByIsBlocked(isBanned);
        return users.stream().map(userMapper::getInfo).collect(Collectors.toList());
    }
    @Override
    public void createStaff(UserReq userReq){
        User staff = userMapper.toEntity(userReq);
        Role role = roleRepository.findByRoleName(userReq.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        staff.setRole(role);
        staff.setPassword(passwordEncoder.encode(userReq.getPassword()));
        staff.setIsVerified(true);
        staff.setIsBlocked(false);
        userRepository.save(staff);
    }

}

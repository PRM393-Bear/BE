package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.entity.User;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;



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

        userRepository.save(user);
    }

    @Override
    public void updatePassword(UUID userId, String oldPassword, String newPassword, String confirmPassword) {
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
}

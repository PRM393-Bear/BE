package com.example.PRM.serviceImpl;

import com.example.PRM.dto.response.UserRes;
import com.example.PRM.entity.User;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.UserMapper;
import org.springframework.stereotype.Service;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.UserService;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    public UserRepository userRepository;
    public UserMapper userMapper;
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserRes getUserById(UUID userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFoundException("User not found with id: " + userId));

        return userMapper.getInfo(user);
    }
}

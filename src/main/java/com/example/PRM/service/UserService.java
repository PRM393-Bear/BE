package com.example.PRM.service;

import com.example.PRM.dto.response.UserRes;

import java.util.UUID;

public interface UserService {
    public UserRes getUserById(UUID userId);
}

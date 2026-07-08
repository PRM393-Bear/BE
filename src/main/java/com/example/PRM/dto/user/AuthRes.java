package com.example.PRM.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthRes {
    private String accessToken;
    private String refreshToken;
}

package com.example.PRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRes {
    private String accessToken;
    private String refreshToken;
}

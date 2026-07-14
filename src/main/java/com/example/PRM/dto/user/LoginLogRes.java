package com.example.PRM.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLogRes {
    private String accessToken;
    private String refreshToken;
    private String username;
    private UUID userId;
    private String organizationStatus = null;
    private String organizationId;
}

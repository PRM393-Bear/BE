package com.example.PRM.service;

import com.example.PRM.entity.RefreshToken;
import com.example.PRM.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RefreshTokenService {
    public RefreshToken rotate(String tokenStr);
    public RefreshToken createRefreshToken(User user);
    public void revokeByUser(User user, HttpServletRequest request);
}

package com.example.PRM.controller;

import com.example.PRM.dto.request.LoginReq;
import com.example.PRM.dto.request.RegisterReq;
import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.AuthRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.PRM.serviceImpl.AuthServiceImpl;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<AuthRes> register(@RequestBody UserReq request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@RequestBody LoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

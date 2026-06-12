package com.example.PRM.controller;

import com.example.PRM.dto.request.LoginReq;
import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.AuthRes;
import com.example.PRM.util.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.PRM.serviceImpl.AuthServiceImpl;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserReq request) {
        authService.registerForMember(request);
        return ResponseEntity.ok("User registered successfully, please login!!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@RequestBody LoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }

}

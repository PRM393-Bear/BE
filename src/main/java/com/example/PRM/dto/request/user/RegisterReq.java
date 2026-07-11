package com.example.PRM.dto.request.user;

import lombok.Data;

@Data
public class RegisterReq {
    private String username;
    private String email;
    private String password;
}

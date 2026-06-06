package com.example.PRM.dto.request;

import lombok.Data;

@Data
public class RegisterReq {
    private String username;
    private String email;
    private String password;
}

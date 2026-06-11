package com.example.PRM.dto.response;

import com.example.PRM.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRes {
    private String token;
}

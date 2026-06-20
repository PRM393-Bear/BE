package com.example.PRM.dto.request;

import com.example.PRM.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReq {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private String roleName;
}

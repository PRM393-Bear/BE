package com.example.PRM.dto.response;

import com.example.PRM.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRes {
    String username;
    String password;
    String email;
    String fullName;
    String phone;
    Role role;
}

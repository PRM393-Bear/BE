package com.example.PRM.dto.response;

import com.example.PRM.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminRes {
    private UUID userId;
    private String userName;
    private String fullName;
    private String email;
    private String logoUrl;
    private String phone;
    private Role role;
    private String address;
}

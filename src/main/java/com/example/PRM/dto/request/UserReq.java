package com.example.PRM.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReq {
    String username;
    String password;
    String email;
    String fullName;
    String phone;
}

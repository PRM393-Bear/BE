package com.example.PRM.mapper;

import com.example.PRM.dto.request.user.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserReq user) {
        User staff = new User();
        staff.setEmail(user.getEmail());
        staff.setPassword(user.getPassword());
        staff.setPhone(user.getPhone());
        staff.setUserName(user.getUsername());
        staff.setFullName(user.getFullName());
        return staff;
    }

    public UserAdminRes mapToUserAdminRes(User user) {
        UserAdminRes uAR = new UserAdminRes();

        uAR.setEmail(user.getEmail());
        uAR.setPhone(user.getPhone());
        uAR.setFullName(user.getFullName());
        uAR.setUserName(user.getUserName());
        uAR.setLogoUrl(user.getLogoUrl());
        uAR.setRole(user.getRole());
        uAR.setUserId(user.getUserId());

        return uAR;
    }

    public UserRes getInfo(User user) {
        UserRes userRes = new UserRes();
        userRes.setEmail(user.getEmail());
        userRes.setPhone(user.getPhone());
        userRes.setFullName(user.getFullName());
        userRes.setRole(user.getRole());
        return userRes;
    }

    public UserLogRes toUserLogRes(User user){
        UserLogRes userLogRes = new UserLogRes();
        userLogRes.setUserId(user.getUserId());
        userLogRes.setUsername(user.getUserName());
        return userLogRes;
    }
}

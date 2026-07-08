package com.example.PRM.mapper;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserAdminRes;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.dto.user.UserLogRes;
import com.example.PRM.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserRes mapToUserRes(User user) {
        UserRes userRes = new UserRes();
        userRes.setEmail(user.getEmail());
        userRes.setPassword(user.getPassword());
        userRes.setPhone(user.getPhone());
        userRes.setUsername(user.getUserName());
        userRes.setFullName(user.getFullName());
        userRes.setRole(user.getRole());
        return userRes;
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

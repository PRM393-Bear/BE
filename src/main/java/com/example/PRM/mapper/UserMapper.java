package com.example.PRM.mapper;

import com.example.PRM.dto.request.UserReq;
import com.example.PRM.dto.response.UserRes;
import com.example.PRM.entity.User;

public class UserMapper {
    public UserRes mapToUserRes(User user) {
        UserRes userRes = new UserRes();
        userRes.setEmail(user.getEmail());
        userRes.setPassword(user.getPassword());
        userRes.setPhone(user.getPhone());
        userRes.setUsername(user.getUserName());
        userRes.setFullName(user.getFullName());
        return userRes;
    }

    public UserReq mapToUserReq(User user) {
        UserReq userReq = new UserReq();
        userReq.setEmail(user.getEmail());
        userReq.setPassword(user.getPassword());
        userReq.setPhone(user.getPhone());
        userReq.setUsername(user.getUserName());
        userReq.setFullName(user.getFullName());
        return userReq;
    }

    public UserRes getInfo(User user) {
        UserRes userRes = new UserRes();
        userRes.setEmail(user.getEmail());
        userRes.setPhone(user.getPhone());
        userRes.setFullName(user.getFullName());
        return userRes;
    }
}

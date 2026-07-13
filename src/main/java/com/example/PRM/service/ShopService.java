package com.example.PRM.service;

import com.example.PRM.dto.request.shop.ShopReq;
import com.example.PRM.dto.response.ShopRes;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface ShopService {
    void createShop(ShopReq shopReq, UserDetails userDetails);
    void updateShop(UUID shopId, ShopReq shopReq);
    void deleteShop(UUID shopId, UserDetails userDetails);
    ShopRes getShop(UUID shopId);
}

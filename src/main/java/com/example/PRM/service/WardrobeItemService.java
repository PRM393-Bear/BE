package com.example.PRM.service;

import com.example.PRM.dto.response.WardrobeItemRes;
import com.example.PRM.status_enum.WardrobeStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface WardrobeItemService {
    public void createWardrobeItem(UserDetails userDetails, UUID productId, HttpServletRequest request);
    public List<WardrobeItemRes> getWardrobeItems(UserDetails userDetails);
    public void deleteWardrobeItem(UserDetails userDetails, UUID wardrobeItemId, HttpServletRequest request);
    public void updateWardrobeItem(UserDetails userDetails, UUID wardrobeItemId, WardrobeStatus status, HttpServletRequest request);
}

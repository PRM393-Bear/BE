package com.example.PRM.service;

import com.example.PRM.dto.response.wardrobeItem.WardrobeItemLogRes;
import com.example.PRM.dto.response.wardrobeItem.WardrobeItemRes;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface WardrobeItemService {
    public WardrobeItemLogRes createWardrobeItem(UserDetails userDetails, UUID productId);
    public List<WardrobeItemRes> getWardrobeItems(UserDetails userDetails);
    public WardrobeItemLogRes deleteWardrobeItem(UserDetails userDetails, UUID wardrobeItemId);
    public WardrobeItemLogRes updateWardrobeItem(UserDetails userDetails, UUID wardrobeItemId, WardrobeStatus status);
}

package com.example.PRM.mapper;

import com.example.PRM.dto.response.WardrobeItemRes;
import com.example.PRM.entity.WardrobeItem;
import org.springframework.stereotype.Component;

@Component
public class WardrobeItemMapper {
    public WardrobeItemRes toResponse(WardrobeItem wardrobeItem){
        WardrobeItemRes wardrobeItemRes = new WardrobeItemRes();
        wardrobeItemRes.setCategory(wardrobeItem.getCategory());
        wardrobeItemRes.setName(wardrobeItem.getName());
        wardrobeItemRes.setImageUrl(wardrobeItem.getImageUrl());
        return wardrobeItemRes;
    }
}

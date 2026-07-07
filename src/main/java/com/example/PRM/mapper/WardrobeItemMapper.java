package com.example.PRM.mapper;

import com.example.PRM.dto.request.DonationRequestCustomReq;
import com.example.PRM.dto.response.WardrobeItemRes;
import com.example.PRM.entity.WardrobeItem;
import com.example.PRM.status_enum.AddedVia;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.stereotype.Component;

@Component
public class WardrobeItemMapper {
    public WardrobeItemRes toResponse(WardrobeItem wardrobeItem){
        WardrobeItemRes wardrobeItemRes = new WardrobeItemRes();

        if (wardrobeItem.getCategory() != null) {
            wardrobeItemRes.setCategory(wardrobeItem.getCategory().getName());
        }

        wardrobeItemRes.setName(wardrobeItem.getName());
        wardrobeItemRes.setImageUrl(wardrobeItem.getImageUrl());
        return wardrobeItemRes;
    }

    public WardrobeItem toEntity(DonationRequestCustomReq wardrobeItemRes){
        WardrobeItem wi = new WardrobeItem();
        wi.setStatus(WardrobeStatus.LISTED);
        wi.setAddedVia(AddedVia.UPLOAD);
        wi.setName(wardrobeItemRes.getItemName());
        wi.setCondition(wardrobeItemRes.getCondition());
        wi.setConditionNote(wardrobeItemRes.getConditionNote());
        return wi;
    }
}

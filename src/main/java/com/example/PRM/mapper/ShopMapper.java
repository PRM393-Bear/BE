package com.example.PRM.mapper;

import com.example.PRM.dto.request.ShopReq;
import com.example.PRM.dto.response.ShopRes;
import com.example.PRM.entity.Shop;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {
    public ShopRes toResponse(Shop shop){
        ShopRes shopRes = new ShopRes();
        shopRes.setShopName(shop.getShopName());
        shopRes.setPhone(shop.getPhone());
        shopRes.setAddress(shop.getAddress());
        shopRes.setLongitude(shop.getLongitude());
        shopRes.setLatitude(shop.getLatitude());
        shopRes.setDescription(shop.getDescription());
        shopRes.setAvtShop(shop.getAvtShop());
        return shopRes;
    }

    public Shop toEntity(ShopReq shopReq){
        Shop shop = new Shop();
        shop.setShopName(shopReq.getShopName());
        shop.setPhone(shopReq.getPhone());
        shop.setAddress(shopReq.getAddress());
        shop.setLongitude(shopReq.getLongitude());
        shop.setLatitude(shopReq.getLatitude());
        shop.setDescription(shopReq.getDescription());
        shop.setAvtShop(shopReq.getAvtShop());
        return shop;
    }
}

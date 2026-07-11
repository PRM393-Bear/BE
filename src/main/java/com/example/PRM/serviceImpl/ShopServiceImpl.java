package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.shop.ShopReq;
import com.example.PRM.dto.response.ShopRes;
import com.example.PRM.entity.Shop;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.ShopMapper;
import com.example.PRM.repository.ShopRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.ShopService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ShopMapper shopMapper;
    public ShopServiceImpl(ShopRepository shopRepository, UserRepository userRepository, ShopMapper shopMapper) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.shopMapper = shopMapper;
    }
    @Override
    public void createShop(ShopReq shopReq, UserDetails userDetails) {
        Shop shop = shopMapper.toEntity(shopReq);
        shop.setOwner(userRepository.findByUserName(userDetails.getUsername()).orElseThrow(()
                -> new NotFoundException("User not found")));
        shopRepository.save(shop);
    }

    @Override
    public void updateShop(UUID shopId, ShopReq shopReq) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(()
                -> new NotFoundException("Shop not found with id: " + shopId));
        if(shopReq.getShopName() != null){
            shop.setShopName(shopReq.getShopName());
        }
        if(shopReq.getAddress() != null){
            shop.setAddress(shopReq.getAddress());
        }
        if(shopReq.getDescription() != null){
            shop.setDescription(shopReq.getDescription());
        }
        if(shopReq.getLatitude() != null && shopReq.getLatitude().compareTo(BigDecimal.ZERO) > 0){
            shop.setLatitude(shopReq.getLatitude());
        }
        if(shopReq.getLongitude() != null && shopReq.getLongitude().compareTo(BigDecimal.ZERO) > 0){
            shop.setLongitude(shopReq.getLongitude());
        }
        if(shopReq.getPhone() != null){
            shop.setPhone(shopReq.getPhone());
        }
        if(shopReq.getShopName() != null){
            shop.setShopName(shopReq.getShopName());
        }
        shopRepository.save(shop);
    }

    @Override
    public void deleteShop(UUID shopId, UserDetails userDetails) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() ->
                new NotFoundException("Shop not found with id: " + shopId));
        if(!shop.getOwner().getUserName().equals(userDetails.getUsername())){
            throw new ForbiddenException("You are not authorized to delete this shop");
        }
        shopRepository.delete(shop);
    }

    @Override
    public ShopRes getShop(UUID shopId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() ->
                new NotFoundException("Shop not found with id: " + shopId));
        return shopMapper.toResponse(shop);
    }
}

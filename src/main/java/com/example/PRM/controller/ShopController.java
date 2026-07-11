package com.example.PRM.controller;

import com.example.PRM.dto.request.shop.ShopReq;
import com.example.PRM.dto.response.ShopRes;
import com.example.PRM.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<String> createShop(
            @RequestBody ShopReq shopReq,
            @AuthenticationPrincipal UserDetails userDetails) {

        shopService.createShop(shopReq, userDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Shop created successfully");
    }

    @PutMapping("/{shopId}")
    public ResponseEntity<String> updateShop(
            @PathVariable UUID shopId,
            @RequestBody ShopReq shopReq) {

        shopService.updateShop(shopId, shopReq);

        return ResponseEntity.ok("Shop updated successfully");
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<String> deleteShop(
            @PathVariable UUID shopId, @AuthenticationPrincipal UserDetails userDetails) {

        shopService.deleteShop(shopId, userDetails);

        return ResponseEntity.ok("Shop deleted successfully");
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopRes> getShop(
            @PathVariable UUID shopId) {

        ShopRes shop = shopService.getShop(shopId);

        return ResponseEntity.ok(shop);
    }
}
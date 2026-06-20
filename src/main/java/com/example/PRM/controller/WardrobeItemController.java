package com.example.PRM.controller;

import com.example.PRM.dto.response.WardrobeItemRes;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wardrobe-items")
public class WardrobeItemController {
    private final WardrobeItemService wardrobeItemService;
    public WardrobeItemController(WardrobeItemService wardrobeItemService) {
        this.wardrobeItemService = wardrobeItemService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> createWardrobeItem(@PathVariable UUID productId, @AuthenticationPrincipal UserDetails userDetails) {
        wardrobeItemService.createWardrobeItem(userDetails, productId);
        return ResponseEntity.ok("Wardrobe item created successfully");
    }

    @GetMapping("/my-wardrobe")
    public ResponseEntity<?> getMyWardrobe(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WardrobeItemRes> lists =
                wardrobeItemService.getWardrobeItems(userDetails);

        if (lists.isEmpty()) {
            return ResponseEntity.ok("Tủ đồ của bạn đang trống, hãy mua thêm sản phẩm để lấp đầy tủ đồ nhé!");
        }

        return ResponseEntity.ok(lists);
    }

    @DeleteMapping("/{wardrobeItemId}")
    public ResponseEntity<?> deleteWardrobeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID wardrobeItemId) {

        wardrobeItemService.deleteWardrobeItem(userDetails, wardrobeItemId);

        return ResponseEntity.ok("Xóa vật phẩm khỏi tủ đồ thành công");
    }

    @PutMapping("/{wardrobeItemId}/status")
    public ResponseEntity<?> updateWardrobeItemStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID wardrobeItemId,
            @RequestParam WardrobeStatus status) {

        wardrobeItemService.updateWardrobeItem(
                userDetails,
                wardrobeItemId,
                status
        );

        return ResponseEntity.ok("Cập nhật trạng thái vật phẩm thành công");
    }
}

package com.example.PRM.controller;

import com.example.PRM.dto.response.wardrobeItem.WardrobeItemLogRes;
import com.example.PRM.dto.response.wardrobeItem.WardrobeItemRes;
import com.example.PRM.service.AuditLogService;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.WardrobeStatus;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditLogService auditLogService;
    public WardrobeItemController(WardrobeItemService wardrobeItemService, AuditLogService auditLogService) {
        this.wardrobeItemService = wardrobeItemService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> createWardrobeItem(@PathVariable UUID productId,
                                                @AuthenticationPrincipal UserDetails userDetails,
                                                HttpServletRequest request) {
        WardrobeItemLogRes res = wardrobeItemService.createWardrobeItem(userDetails, productId);
        auditLogService.log("CREATE_WARDROBE_ITEM",
                res.getName(),
                res.getId().toString(),
                "User create wardrobe item successfully",
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );
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
            @PathVariable UUID wardrobeItemId,
            HttpServletRequest request) {

        WardrobeItemLogRes res = wardrobeItemService.deleteWardrobeItem(userDetails, wardrobeItemId);
        auditLogService.log("DELETE_WARDROBE_ITEM",
                res.getName(),
                wardrobeItemId.toString(),
                "User delete wardrobe item successfully",
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );

        return ResponseEntity.ok("Xóa vật phẩm khỏi tủ đồ thành công");
    }

    @PutMapping("/{wardrobeItemId}/status")
    public ResponseEntity<?> updateWardrobeItemStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID wardrobeItemId,
            @RequestParam WardrobeStatus status,
            HttpServletRequest request) {

        WardrobeItemLogRes res = wardrobeItemService.updateWardrobeItem(
                userDetails,
                wardrobeItemId,
                status
        );
        auditLogService.log("UPDATE_WARDROBE_ITEM",
                res.getName(),
                res.getId().toString(),
                "User update wardrobe item successfully",
                "SUCCESS",
                res.getUserId(),
                res.getUsername(),
                request
        );

        return ResponseEntity.ok("Cập nhật trạng thái vật phẩm thành công");
    }
}

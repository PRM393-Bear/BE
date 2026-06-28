package com.example.PRM.controller;

import com.example.PRM.dto.request.CreateOrderReq;
import com.example.PRM.dto.response.ApiResponse;
import com.example.PRM.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody CreateOrderReq req) {
        var result = orderService.createOrder(userDetails, req);
        return ResponseEntity.ok(new ApiResponse(200, "Order created successfully"));
    }

    @PutMapping("/confirm")
    public ResponseEntity<?> confirmOrder(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam UUID orderId) {

        var result = orderService.confirmOrder(userDetails, orderId);
        return ResponseEntity.ok(new ApiResponse(200, "Order status updated successfully."));
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable UUID orderId,
                                       @RequestParam String trackingCode) {
        var result = orderService.enterShipOrder(userDetails, orderId, trackingCode);
        return ResponseEntity.ok(new ApiResponse(200, "Order status updated successfully."));
    }

    @PutMapping("/{orderId}/receive")
    public ResponseEntity<?> confirmReceived(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable UUID orderId,
                                             @RequestParam(required = false) String deliveryPhotoUrl) {
        var result = orderService.confirmReceived(userDetails, orderId, deliveryPhotoUrl);
        return ResponseEntity.ok(new ApiResponse(200, "Order status updated successfully."));
    }

    @GetMapping("/buyer")
    public ResponseEntity<?> getOrdersByBuyer(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrdersByBuyer(userDetails));
    }

    @GetMapping("/seller")
    public ResponseEntity<?> getOrdersBySeller(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrdersBySeller(userDetails));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(userDetails, orderId));
    }
}

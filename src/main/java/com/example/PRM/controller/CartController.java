package com.example.PRM.controller;

import com.example.PRM.dto.response.ApiResponse;
import com.example.PRM.dto.response.CartItemRes;
import com.example.PRM.dto.response.CartRes;
import com.example.PRM.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam UUID productId) {

        cartService.addProductToCart(userDetails, productId);

        return ResponseEntity.ok(new ApiResponse(200, "Add to cart successfully"));
    }

    @GetMapping
    public ResponseEntity<CartRes> getCart(@AuthenticationPrincipal UserDetails userDetails) {

        var cartData = cartService.getCartByUserId(userDetails);

        return ResponseEntity.ok(cartData);
    }

    @DeleteMapping
    public ResponseEntity<?> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam UUID cartItemId
    ) {
        cartService.removeCartItem(userDetails, cartItemId);
        return ResponseEntity.ok(new ApiResponse(200, "Cart Item was deleted successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails);

        return ResponseEntity.ok(new ApiResponse(200, "Cart was cleared successfully"));
    }
}

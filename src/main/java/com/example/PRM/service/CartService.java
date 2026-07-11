package com.example.PRM.service;

import com.example.PRM.dto.response.CartRes;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface CartService {
    CartRes getCartByUserId(UserDetails userDetails);
    CartRes addProductToCart(UserDetails userDetails, UUID productId);
    void removeCartItem(UserDetails userDetails, UUID cartItemId);
    void clearCart(UserDetails userDetails);
}

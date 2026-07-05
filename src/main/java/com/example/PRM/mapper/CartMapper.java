package com.example.PRM.mapper;

import com.example.PRM.dto.response.CartItemRes;
import com.example.PRM.dto.response.CartRes;
import com.example.PRM.entity.Cart;
import com.example.PRM.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartRes mapToResponseDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        BigDecimal total = BigDecimal.ZERO;

        List<CartItemRes> itemDTOs = cart.getCartItems().stream().map(item -> {
            Product p = item.getProduct();
            return CartItemRes.builder()
                    .cartItemId(item.getId())
                    .productId(p.getId())
                    .productName(p.getTitle())
                    .price(BigDecimal.valueOf(p.getPrice()))
                    .build();
        }).collect(Collectors.toList());

        for (CartItemRes item : itemDTOs) {
            if (item.getPrice() != null) {
                total = total.add(item.getPrice());
            }
        }

        return CartRes.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getUserId())
                .items(itemDTOs)
                .totalPrice(total)
                .build();
    }
}

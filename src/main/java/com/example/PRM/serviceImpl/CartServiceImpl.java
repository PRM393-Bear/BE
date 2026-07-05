package com.example.PRM.serviceImpl;

import com.example.PRM.dto.response.CartRes;
import com.example.PRM.entity.Cart;
import com.example.PRM.entity.CartItem;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.CartMapper;
import com.example.PRM.repository.CartItemRepository;
import com.example.PRM.repository.CartRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    public CartRes getCartByUserId(UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        return cartMapper.mapToResponseDTO(cart);
    }

    @Override
    @Transactional
    public CartRes addProductToCart(UserDetails userDetails, UUID productId) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getStatus() != com.example.PRM.status_enum.ProductStatus.AVAILABLE) {
            throw new BadRequestException("This product is no longer available for purchase!");
        }

        boolean alreadyInCart = cart.getCartItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (alreadyInCart) {
            throw new BadRequestException("This product is already in your cart!");
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        cart.getCartItems().add(newItem);
        cartRepository.save(cart);

        return cartMapper.mapToResponseDTO(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(UserDetails userDetails, UUID cartItemId) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        boolean removed = cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));

        if (!removed) {
            throw new NotFoundException("CartItem not found in your cart");
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}

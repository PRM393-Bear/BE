package com.example.PRM.serviceImpl;

import com.example.PRM.dto.response.CartRes;
import com.example.PRM.entity.Cart;
import com.example.PRM.entity.CartItem;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.CartMapper;
import com.example.PRM.repository.CartRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.status_enum.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private UserDetails userDetails;

    private User user;
    private Cart cart;
    private Product product;
    private UUID cartItemId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("testuser");

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setStatus(ProductStatus.AVAILABLE);

        cartItemId = UUID.randomUUID();

        lenient().when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void getCartByUserId_ShouldReturnCartRes_WhenUserExists() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));
        when(cartMapper.mapToResponseDTO(cart)).thenReturn(CartRes.builder().build());

        CartRes response = cartService.getCartByUserId(userDetails);

        assertNotNull(response);
        verify(cartRepository, times(1)).findByUser_UserId(user.getUserId());
    }

    @Test
    void getCartByUserId_ShouldCreateCart_WhenCartDoesNotExist() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.mapToResponseDTO(cart)).thenReturn(CartRes.builder().build());

        CartRes response = cartService.getCartByUserId(userDetails);

        assertNotNull(response);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCartByUserId_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.getCartByUserId(userDetails));
    }

    @Test
    void addProductToCart_ShouldAddProduct_WhenValidRequest() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.mapToResponseDTO(cart)).thenReturn(CartRes.builder().build());

        CartRes response = cartService.addProductToCart(userDetails, product.getId());

        assertNotNull(response);
        assertEquals(1, cart.getCartItems().size());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addProductToCart_ShouldThrowBadRequest_WhenProductNotAvailable() {
        product.setStatus(ProductStatus.SOLD);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> cartService.addProductToCart(userDetails, product.getId()));
    }

    @Test
    void addProductToCart_ShouldThrowBadRequest_WhenProductAlreadyInCart() {
        CartItem item = new CartItem();
        item.setProduct(product);
        cart.getCartItems().add(item);

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> cartService.addProductToCart(userDetails, product.getId()));
    }

    @Test
    void removeCartItem_ShouldRemoveItem_WhenItemExists() {
        CartItem item = new CartItem();
        item.setId(cartItemId);
        cart.getCartItems().add(item);

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));

        cartService.removeCartItem(userDetails, cartItemId);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void removeCartItem_ShouldThrowNotFound_WhenItemNotInCart() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));

        assertThrows(NotFoundException.class, () -> cartService.removeCartItem(userDetails, cartItemId));
    }

    @Test
    void clearCart_ShouldClearAllItems() {
        CartItem item = new CartItem();
        cart.getCartItems().add(item);

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(cart));

        cartService.clearCart(userDetails);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }
}

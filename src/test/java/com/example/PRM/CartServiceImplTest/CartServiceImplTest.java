package com.example.PRM.CartServiceImplTest;

import com.example.PRM.dto.response.CartRes;
import com.example.PRM.entity.*;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.CartMapper;
import com.example.PRM.repository.CartItemRepository;
import com.example.PRM.repository.CartRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.CartServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;
    private CartRes cartRes;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("john");

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setStatus(ProductStatus.AVAILABLE);

        cartRes = new CartRes();

        when(userDetails.getUsername()).thenReturn("john");
    }

    // =====================================================
    // getCartByUserId
    // =====================================================

    @Test
    void getCartByUserId_UserNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.getCartByUserId(userDetails)
        );
    }

    @Test
    void getCartByUserId_CartExists() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        when(cartMapper.mapToResponseDTO(cart))
                .thenReturn(cartRes);

        CartRes result = cartService.getCartByUserId(userDetails);

        assertEquals(cartRes, result);
    }

    @Test
    void getCartByUserId_CreateCartWhenMissing() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(i -> i.getArgument(0));

        when(cartMapper.mapToResponseDTO(any(Cart.class)))
                .thenReturn(cartRes);

        CartRes result = cartService.getCartByUserId(userDetails);

        assertNotNull(result);
    }

    // =====================================================
    // addProductToCart
    // =====================================================

    @Test
    void addProductToCart_UserNotFound() {

        assertThrows(
                NotFoundException.class,
                () -> cartService.addProductToCart(userDetails, UUID.randomUUID())
        );
    }

    @Test
    void addProductToCart_ProductNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.addProductToCart(userDetails, UUID.randomUUID())
        );
    }

    @Test
    void addProductToCart_ProductUnavailable() {

        product.setStatus(ProductStatus.SOLD);

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        assertThrows(
                BadRequestException.class,
                () -> cartService.addProductToCart(userDetails, product.getId())
        );
    }

    @Test
    void addProductToCart_ProductAlreadyInCart() {

        CartItem item = new CartItem();
        item.setProduct(product);

        cart.getCartItems().add(item);

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        assertThrows(
                BadRequestException.class,
                () -> cartService.addProductToCart(userDetails, product.getId())
        );
    }

    @Test
    void addProductToCart_Success() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(any()))
                .thenReturn(cart);

        when(cartMapper.mapToResponseDTO(cart))
                .thenReturn(cartRes);

        CartRes result =
                cartService.addProductToCart(userDetails, product.getId());

        assertNotNull(result);
        assertEquals(1, cart.getCartItems().size());
    }

    @Test
    void addProductToCart_CreateCartAndSuccess() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(i -> i.getArgument(0));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        when(cartMapper.mapToResponseDTO(any()))
                .thenReturn(cartRes);

        CartRes result =
                cartService.addProductToCart(userDetails, product.getId());

        assertNotNull(result);
    }

    // =====================================================
    // removeCartItem
    // =====================================================

    @Test
    void removeCartItem_UserNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.removeCartItem(userDetails, UUID.randomUUID())
        );
    }

    @Test
    void removeCartItem_CartNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.removeCartItem(userDetails, UUID.randomUUID())
        );
    }

    @Test
    void removeCartItem_ItemNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        assertThrows(
                NotFoundException.class,
                () -> cartService.removeCartItem(userDetails, UUID.randomUUID())
        );
    }

    @Test
    void removeCartItem_Success() {

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());

        cart.getCartItems().add(item);

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.removeCartItem(userDetails, item.getId());

        verify(cartRepository).save(cart);
    }

    // =====================================================
    // clearCart
    // =====================================================

    @Test
    void clearCart_UserNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.clearCart(userDetails)
        );
    }

    @Test
    void clearCart_CartNotFound() {

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.clearCart(userDetails)
        );
    }

    @Test
    void clearCart_Success() {

        CartItem item = new CartItem();
        cart.getCartItems().add(item);

        when(userRepository.findByUserName("john"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.clearCart(userDetails);

        assertTrue(cart.getCartItems().isEmpty());

        verify(cartRepository).save(cart);
    }
}
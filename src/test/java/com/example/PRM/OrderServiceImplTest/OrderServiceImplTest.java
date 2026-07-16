package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.order.CreateOrderReq;
import com.example.PRM.entity.Order;
import com.example.PRM.entity.OrderItem;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.repository.OrderRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private WardrobeItemService wardrobeItemService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private UserDetails userDetails;
    @Mock private HttpServletRequest request;

    private User buyer;
    private User seller;
    private Product product;
    private Order order;
    private OrderItem orderItem;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        buyer = new User();
        buyer.setUserId(UUID.randomUUID());
        buyer.setUserName("buyerUser");
        buyer.setFullName("Buyer Name");

        seller = new User();
        seller.setUserId(UUID.randomUUID());
        seller.setUserName("sellerUser");

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(seller);
        product.setPrice(100L);

        orderItem = new OrderItem();
        orderItem.setProduct(product);

        order = new Order();
        order.setId(orderId);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(new ArrayList<>());
        order.getOrderItems().add(orderItem);

        lenient().when(userDetails.getUsername()).thenReturn("buyerUser");
    }

    @Test
    void createOrder_ShouldCreate_WhenValidRequest() {
        CreateOrderReq req = new CreateOrderReq();
        req.setProductId(product.getId());

        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.createOrder(userDetails, req);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(buyer, result.getBuyer());
        assertEquals(seller, result.getSeller());
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void confirmOrder_ShouldConfirm_WhenSeller() {
        when(userDetails.getUsername()).thenReturn("sellerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.confirmOrder(userDetails, orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void confirmOrder_ShouldThrowForbidden_WhenNotSeller() {
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () -> orderService.confirmOrder(userDetails, orderId));
    }

    @Test
    void enterShipOrder_ShouldShip_WhenSellerAndProcessing() {
        order.setStatus(OrderStatus.PROCESSING);
        when(userDetails.getUsername()).thenReturn("sellerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.enterShipOrder(userDetails, orderId, "TRACK123");

        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPING, result.getStatus());
        assertEquals("TRACK123", result.getTrackingCode());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void enterShipOrder_ShouldThrowBadRequest_WhenNotProcessing() {
        order.setStatus(OrderStatus.PENDING);
        when(userDetails.getUsername()).thenReturn("sellerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.enterShipOrder(userDetails, orderId, "TRACK123"));
    }

    @Test
    void confirmReceived_ShouldConfirm_WhenBuyerAndShipping() {
        order.setStatus(OrderStatus.SHIPPING);
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.confirmReceived(userDetails, orderId, "photo.jpg", request);

        assertNotNull(result);
        assertEquals(OrderStatus.RECEIVED, result.getStatus());
        assertEquals("photo.jpg", result.getDeliveryPhotoUrl());
        verify(wardrobeItemService, times(1)).createWardrobeItem(any(), any());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void confirmReceived_ShouldThrowBadRequest_WhenNotShipping() {
        order.setStatus(OrderStatus.PENDING);
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));

        assertThrows(BadRequestException.class, () -> orderService.confirmReceived(userDetails, orderId, "photo.jpg", request));
    }
}

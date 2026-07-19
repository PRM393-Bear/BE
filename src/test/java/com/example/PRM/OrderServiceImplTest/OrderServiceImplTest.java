package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.order.CreateOrderReq;
import com.example.PRM.dto.response.OrderRes;
import com.example.PRM.entity.Order;
import com.example.PRM.entity.OrderItem;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.OrderMapper;
import com.example.PRM.repository.OrderRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.NotificationService;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.OrderStatus;
import com.example.PRM.status_enum.ProductStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private WardrobeItemService wardrobeItemService;
    @Mock private NotificationService notificationService;
    @Mock private OrderMapper orderMapper;
    @Mock private AuditLogServiceImpl auditLogService;
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

    // CREATE ORDER
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
        assertEquals(ProductStatus.SOLD, product.getStatus());
        assertEquals(buyer, result.getBuyer());
        assertEquals(seller, result.getSeller());
        assertEquals(BigDecimal.valueOf(100), result.getTotalAmount());
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void createOrder_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.empty());
        CreateOrderReq req = new CreateOrderReq();
        
        assertThrows(NotFoundException.class, () -> orderService.createOrder(userDetails, req));
    }

    @Test
    void createOrder_ShouldThrowNotFound_WhenProductNotFound() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());
        CreateOrderReq req = new CreateOrderReq();
        req.setProductId(product.getId());

        assertThrows(NotFoundException.class, () -> orderService.createOrder(userDetails, req));
    }

    // CONFIRM ORDER
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
    void confirmOrder_ShouldThrowNotFound_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.confirmOrder(userDetails, orderId));
    }

    @Test
    void confirmOrder_ShouldThrowForbidden_WhenNotSeller() {
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () -> orderService.confirmOrder(userDetails, orderId));
    }

    // ENTER SHIP ORDER
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
    void enterShipOrder_ShouldThrowNotFound_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.enterShipOrder(userDetails, orderId, "TRACK123"));
    }

    @Test
    void enterShipOrder_ShouldThrowForbidden_WhenNotSeller() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("otherUser");
        assertThrows(ForbiddenException.class, () -> orderService.enterShipOrder(userDetails, orderId, "TRACK123"));
    }

    @Test
    void enterShipOrder_ShouldThrowBadRequest_WhenNotProcessing() {
        order.setStatus(OrderStatus.PENDING);
        when(userDetails.getUsername()).thenReturn("sellerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.enterShipOrder(userDetails, orderId, "TRACK123"));
    }

    // CONFIRM RECEIVED
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
    void confirmReceived_ShouldThrowNotFound_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.confirmReceived(userDetails, orderId, "p", request));
    }

    @Test
    void confirmReceived_ShouldThrowForbidden_WhenNotBuyer() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("sellerUser");
        assertThrows(ForbiddenException.class, () -> orderService.confirmReceived(userDetails, orderId, "p", request));
    }

    @Test
    void confirmReceived_ShouldThrowNotFound_WhenUserNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.confirmReceived(userDetails, orderId, "p", request));
    }

    @Test
    void confirmReceived_ShouldThrowBadRequest_WhenNotShipping() {
        order.setStatus(OrderStatus.PENDING);
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));

        assertThrows(BadRequestException.class, () -> orderService.confirmReceived(userDetails, orderId, "photo.jpg", request));
    }

    @Test
    void confirmReceived_ShouldConfirm_WhenPhotoNull() {
        order.setStatus(OrderStatus.SHIPPING);
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.confirmReceived(userDetails, orderId, null, request);

        assertNotNull(result);
        assertNull(result.getDeliveryPhotoUrl());
    }

    @Test
    void confirmReceived_ShouldConfirm_WhenPhotoEmpty() {
        order.setStatus(OrderStatus.SHIPPING);
        when(userDetails.getUsername()).thenReturn("buyerUser");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.confirmReceived(userDetails, orderId, "", request);

        assertNotNull(result);
        assertNull(result.getDeliveryPhotoUrl());
    }

    // GET ORDERS BY BUYER
    @Test
    void getOrdersByBuyer_ShouldReturnList() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerOrderByCreatedAtDesc(buyer)).thenReturn(Collections.singletonList(order));
        when(orderMapper.toResponse(any())).thenReturn(new OrderRes());

        List<OrderRes> res = orderService.getOrdersByBuyer(userDetails);

        assertEquals(1, res.size());
    }

    @Test
    void getOrdersByBuyer_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrdersByBuyer(userDetails));
    }

    // GET ORDERS BY SELLER
    @Test
    void getOrdersBySeller_ShouldReturnList() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(seller));
        when(orderRepository.findBySeller(seller)).thenReturn(Collections.singletonList(order));
        when(orderMapper.toResponse(any())).thenReturn(new OrderRes());

        List<OrderRes> res = orderService.getOrdersBySeller(userDetails);

        assertEquals(1, res.size());
    }

    @Test
    void getOrdersBySeller_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrdersBySeller(userDetails));
    }

    // GET ORDER BY ID
    @Test
    void getOrderById_ShouldReturnOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        Order result = orderService.getOrderById(userDetails, orderId);
        assertEquals(orderId, result.getId());
    }

    @Test
    void getOrderById_ShouldThrowNotFound_WhenNotExists() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrderById(userDetails, orderId));
    }

    // GET ORDER HISTORY
    @Test
    void getOrderHistory_ShouldReturnWithStatus() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerAndStatusOrderByCreatedAtDesc(buyer, OrderStatus.PENDING)).thenReturn(Collections.singletonList(order));
        when(orderMapper.toResponse(any())).thenReturn(new OrderRes());

        List<OrderRes> res = orderService.getOrderHistory(userDetails, OrderStatus.PENDING);
        assertEquals(1, res.size());
    }

    @Test
    void getOrderHistory_ShouldReturnWithoutStatus() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerOrderByCreatedAtDesc(buyer)).thenReturn(Collections.singletonList(order));
        when(orderMapper.toResponse(any())).thenReturn(new OrderRes());

        List<OrderRes> res = orderService.getOrderHistory(userDetails, null);
        assertEquals(1, res.size());
    }

    @Test
    void getOrderHistory_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findByUserName("buyerUser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrderHistory(userDetails, OrderStatus.PENDING));
    }

    // AUTO PROCESS COMPLETE ORDER
    @Test
    void autoProcessCompleteOrder_ShouldProcessShippingOrders() {
        order.setStatus(OrderStatus.SHIPPING);
        when(orderRepository.findByStatusAndUpdatedAtBefore(eq(OrderStatus.SHIPPING), any())).thenReturn(Collections.singletonList(order));

        orderService.autoProcessCompleteOrder();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(wardrobeItemService, times(1)).createWardrobeItem(any(), eq(product.getId()));
        verify(orderRepository, times(1)).save(order);
        verify(eventPublisher, times(2)).publishEvent(any());
    }

    // UPDATE PICKUP PHOTO
    @Test
    void updatePickupPhoto_ShouldUpdate_WhenSeller() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("sellerUser");
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updatePickupPhoto(userDetails, orderId, "pickup.jpg");

        assertEquals("pickup.jpg", result.getPickupPhotoUrl());
    }

    @Test
    void updatePickupPhoto_ShouldThrowNotFound_WhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.updatePickupPhoto(userDetails, orderId, "p.jpg"));
    }

    @Test
    void updatePickupPhoto_ShouldThrowBadRequest_WhenNotSeller() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("otherUser");
        assertThrows(BadRequestException.class, () -> orderService.updatePickupPhoto(userDetails, orderId, "p.jpg"));
    }
}

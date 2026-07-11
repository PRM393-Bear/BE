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
import com.example.PRM.service.OrderService;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.OrderStatus;
import com.example.PRM.status_enum.ProductStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final WardrobeItemService wardrobeItemService;
    private final NotificationService notificationService;
    private final OrderMapper orderMapper;
    private final AuditLogServiceImpl auditLogService;

    @Override
    @Transactional
    public Order createOrder(UserDetails userDetails, CreateOrderReq req) {

        User buyer = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        product.setStatus(ProductStatus.SOLD);
        productRepository.save(product);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(product.getSeller());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        order.setTotalAmount(unitPrice);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setSubtotal(unitPrice);

        order.getOrderItems().add(orderItem);

        String title = "Đơn hàng mới!";
        String message = "Bạn vừa nhận được một đơn đặt hàng mới từ người dùng " + buyer.getFullName() + ".";

        notificationService.sendNotification(
                order.getSeller().getUserId(),
                title,
                message,
                "ORDER"
        );

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order confirmOrder(UserDetails userDetails, UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!order.getSeller().getUserName().equals(userDetails.getUsername())) {
            throw new ForbiddenException("You do not have permission to perform this action on this order.");
        }

        order.setStatus(OrderStatus.PROCESSING);

        notificationService.sendNotification(
                order.getBuyer().getUserId(),
                "Đơn hàng đã được xác nhận",
                "Người bán đã xác nhận đơn hàng của bạn.",
                "ORDER"
        );

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order enterShipOrder(UserDetails userDetails, UUID orderId, String trackingCode) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!order.getSeller().getUserName().equals(userDetails.getUsername())) {
            throw new ForbiddenException("You do not have permission to perform this action on this order.");
        }

        if (order.getStatus() == OrderStatus.PROCESSING) {

            order.setStatus(OrderStatus.SHIPPING);
            order.setTrackingCode(trackingCode);

            notificationService.sendNotification(
                    order.getBuyer().getUserId(),
                    "Đơn hàng đang được giao",
                    "Đơn hàng của bạn đang được giao với mã vận đơn: " + trackingCode,
                    "ORDER"
            );

            return orderRepository.save(order);
        } else {
            throw new BadRequestException("Your order not status PROCESSING");
        }
    }

    @Override
    @Transactional
    public Order confirmReceived(UserDetails userDetails, UUID orderId, String deliveryPhotoUrl, HttpServletRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getBuyer().getUserName().equals(userDetails.getUsername())) {
            throw new ForbiddenException("You do not have permission to perform this action on this order.");
        }

        User buyer = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (order.getStatus() == OrderStatus.SHIPPING) {

            order.setStatus(OrderStatus.RECEIVED);

            if (deliveryPhotoUrl != null && !deliveryPhotoUrl.isEmpty()) {
                order.setDeliveryPhotoUrl(deliveryPhotoUrl);
            }

            for(OrderItem item : order.getOrderItems()) {
                wardrobeItemService.createWardrobeItem(userDetails, item.getProduct().getId());
            }

        } else  {
            throw new BadRequestException("The order is not in the SHIPPING status.");
        }

        return orderRepository.save(order);
    }

    @Override
    public List<OrderRes> getOrdersByBuyer(UserDetails userDetails) {
        User buyer = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderRes> getOrdersBySeller(UserDetails userDetails) {
        User seller = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findBySeller(seller)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrderById(UserDetails userDetails, UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Override
    public List<OrderRes> getOrderHistory(UserDetails userDetails, OrderStatus status) {

        User buyer = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Order> orders;

        if (status != null) {
            orders = orderRepository.findByBuyerAndStatusOrderByCreatedAtDesc(buyer, status);
        } else {
            orders = orderRepository.findByBuyerOrderByCreatedAtDesc(buyer);
        }
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Override
    @Transactional
    public void autoProcessCompleteOrder() {

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Order> shippingOrders = orderRepository.findByStatusAndUpdatedAtBefore(OrderStatus.SHIPPING, sevenDaysAgo);

        for (Order order : shippingOrders) {
            order.setStatus(OrderStatus.COMPLETED);
            UserDetails mockUserDetails = org.springframework.security.core.userdetails.User
                    .withUsername(order.getBuyer().getUserName())
                    .password("")
                    .authorities("USER")
                    .build();
            for (OrderItem item : order.getOrderItems()) {
                wardrobeItemService.createWardrobeItem(mockUserDetails, item.getProduct().getId());
            }
            orderRepository.save(order);
        }

    }

    @Override
    public Order updatePickupPhoto(UserDetails userDetails, UUID orderId, String photoUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (!order.getSeller().getUserName().equals(userDetails.getUsername())) {
            throw new BadRequestException("You don't have permission to update this order's pickup photo");
        }

        order.setPickupPhotoUrl(photoUrl);
        return orderRepository.save(order);
    }
}

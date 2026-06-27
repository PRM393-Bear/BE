package com.example.PRM.serviceImpl;

import com.example.PRM.dto.request.CreateOrderReq;
import com.example.PRM.entity.Order;
import com.example.PRM.entity.OrderItem;
import com.example.PRM.entity.Product;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.OrderRepository;
import com.example.PRM.repository.ProductRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.OrderService;
import com.example.PRM.service.WardrobeItemService;
import com.example.PRM.status_enum.OrderStatus;
import com.example.PRM.status_enum.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final WardrobeItemService wardrobeItemService;

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
        BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));
        order.setTotalAmount(subTotal);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(req.getQuantity());
        orderItem.setUnitPrice(unitPrice);
        orderItem.setSubtotal(subTotal);

        order.getOrderItems().add(orderItem);

        // Code here: send notification for seller

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

            return orderRepository.save(order);
        } else {
            throw new BadRequestException("Your order not status PROCESSING");
        }
    }

    @Override
    @Transactional
    public Order confirmReceived(UserDetails userDetails, UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getBuyer().getUserName().equals(userDetails.getUsername())) {
            throw new ForbiddenException("You do not have permission to perform this action on this order.");
        }

        User buyer = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (order.getStatus() == OrderStatus.SHIPPING) {

            order.setStatus(OrderStatus.COMPLETED);

            for(OrderItem item : order.getOrderItems()) {
                wardrobeItemService.createWardrobeItem(userDetails, item.getProduct().getId());
            }

        } else  {
            throw new BadRequestException("The order is not in the SHIPPING status.");
        }

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByBuyer(UserDetails userDetails) {
        User buyer = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findByBuyer(buyer);
    }

    @Override
    public List<Order> getOrdersBySeller(UserDetails userDetails) {
        User seller = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findBySeller(seller);
    }

    @Override
    public Order getOrderById(UserDetails userDetails, UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
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
}

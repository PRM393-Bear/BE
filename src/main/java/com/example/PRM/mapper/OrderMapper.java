package com.example.PRM.mapper;

import com.example.PRM.dto.response.OrderItemRes;
import com.example.PRM.dto.response.OrderRes;


import com.example.PRM.entity.Order;
import com.example.PRM.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderRes toResponse(Order order) {
        OrderRes res = new OrderRes();
        res.setId(order.getId());
        res.setTotalAmount(order.getTotalAmount());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());
        res.setUpdatedAt(order.getUpdatedAt());
        res.setTrackingCode(order.getTrackingCode());
        res.setDeliveryPhotoUrl(order.getDeliveryPhotoUrl());

        if (order.getBuyer() != null) {
            res.setBuyerId(order.getBuyer().getUserId());
            res.setBuyerName(order.getBuyer().getFullName());
        }

        if (order.getSeller() != null) {
            res.setSellerId(order.getSeller().getUserId());
            res.setSellerName(order.getSeller().getFullName());
        }

        if (order.getOrderItems() != null) {
            List<OrderItemRes> itemResList = order.getOrderItems().stream()
                    .map(this::toOrderItemRes)
                    .collect(Collectors.toList());
            res.setItems(itemResList);
        }

        return res;
    }

    private OrderItemRes toOrderItemRes(OrderItem item) {
        OrderItemRes res = new OrderItemRes();
        res.setId(item.getId());
        res.setUnitPrice(item.getUnitPrice());

        if (item.getProduct() != null) {
            res.setProductId(item.getProduct().getId());
            res.setProductTitle(item.getProduct().getTitle());

            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                res.setProductImage(item.getProduct().getImages().get(0));
            }
        }
        return res;
    }
}

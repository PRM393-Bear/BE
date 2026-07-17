package com.example.PRM.review;

import com.example.PRM.dto.request.review.ReviewReq;
import com.example.PRM.entity.Order;
import com.example.PRM.entity.ProductReview;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.OrderRepository;
import com.example.PRM.repository.ProductReviewRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.ReviewServiceImpl;
import com.example.PRM.status_enum.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReviewServiceImpl}.
 * Covers every branch of createReview: order not found, not the buyer,
 * order not yet RECEIVED, already reviewed, reviewer not found, and success.
 *
 * NOTE: assumes OrderStatus has a non-RECEIVED value named PENDING to exercise
 * the "order not completed" branch — adjust if your enum uses a different name.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ProductReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    private ReviewServiceImpl reviewService;

    private UUID orderId;
    private User buyer;
    private Order order;
    private ReviewReq req;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, orderRepository, userRepository);

        orderId = UUID.randomUUID();

        buyer = new User();
        buyer.setUserName("john.doe");

        order = new Order();
        order.setId(orderId);
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.RECEIVED);

        req = new ReviewReq();
        req.setOrderId(orderId);
        req.setRating((short) 5);
        req.setComment("Great product!");
    }

    @Test
    void createReview_orderNotFound_throwsNotFoundException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reviewService.createReview(userDetails, req));
        assertTrue(ex.getMessage().contains("Order không tồn tại"));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_notTheBuyer_throwsForbiddenException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("someone.else");

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> reviewService.createReview(userDetails, req));
        assertTrue(ex.getMessage().contains("Chỉ người mua"));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_orderNotReceived_throwsBadRequestException() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("john.doe");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reviewService.createReview(userDetails, req));
        assertTrue(ex.getMessage().contains("chưa hoàn thành"));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_alreadyReviewed_throwsBadRequestException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> reviewService.createReview(userDetails, req));
        assertTrue(ex.getMessage().contains("đã đánh giá"));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_reviewerNotFound_throwsNotFoundException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(false);
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reviewService.createReview(userDetails, req));
        assertTrue(ex.getMessage().contains("User không tồn tại"));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_success_savesReviewWithCorrectFields() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userDetails.getUsername()).thenReturn("john.doe");
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(false);
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(buyer));

        reviewService.createReview(userDetails, req);

        ArgumentCaptor<ProductReview> captor = ArgumentCaptor.forClass(ProductReview.class);
        verify(reviewRepository).save(captor.capture());
        ProductReview saved = captor.getValue();

        assertSame(order, saved.getOrder());
        assertSame(buyer, saved.getReviewer());
        assertEquals((short) 5, saved.getRating());
        assertEquals("Great product!", saved.getComment());
    }
}

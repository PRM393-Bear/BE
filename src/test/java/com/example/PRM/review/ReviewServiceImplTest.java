package com.example.PRM.review;

import com.example.PRM.dto.request.review.ReviewReq;
import com.example.PRM.dto.response.review.ReviewRes;
import com.example.PRM.entity.Order;
import com.example.PRM.entity.OrderItem;
import com.example.PRM.entity.Product;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReviewServiceImpl}.
 * Covers 100% of methods and branches:
 *  - createReview: order not found / not buyer / not RECEIVED / already reviewed / success
 *  - getReviewsForSeller: empty list, orderItems null, orderItems empty,
 *    orderItems populated, reviewer null, reviewer populated
 *
 * Note: ProductReview.rating and ReviewReq.rating are both Short (not int),
 * and createdAt is OffsetDateTime (not LocalDateTime) - matched here exactly
 * to avoid ambiguous overload / type mismatch compile errors.
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

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID orderId;
    private User buyer;
    private Order order;
    private ReviewReq reviewReq;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        buyer = mock(User.class);
        order = mock(Order.class);

        reviewReq = new ReviewReq();
        reviewReq.setOrderId(orderId);
        reviewReq.setRating((short) 5);
        reviewReq.setComment("Great product!");
    }

    // ==================== createReview ====================

    @Nested
    @DisplayName("createReview")
    class CreateReview {

        @Test
        @DisplayName("Order not found -> throws NotFoundException")
        void order_not_found_throws() {
            when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> reviewService.createReview(userDetails, reviewReq));

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Requester is not the buyer -> throws ForbiddenException")
        void not_buyer_throws_forbidden() {
            when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
            when(order.getBuyer()).thenReturn(buyer);
            when(buyer.getUserName()).thenReturn("real-buyer");
            when(userDetails.getUsername()).thenReturn("someone-else");

            assertThrows(ForbiddenException.class,
                    () -> reviewService.createReview(userDetails, reviewReq));

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Order status not RECEIVED -> throws BadRequestException")
        void order_not_received_throws() {
            when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
            when(order.getBuyer()).thenReturn(buyer);
            when(buyer.getUserName()).thenReturn("buyer1");
            when(userDetails.getUsername()).thenReturn("buyer1");
            when(order.getStatus()).thenReturn(OrderStatus.PENDING);

            assertThrows(BadRequestException.class,
                    () -> reviewService.createReview(userDetails, reviewReq));

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Order already reviewed -> throws BadRequestException")
        void already_reviewed_throws() {
            when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
            when(order.getBuyer()).thenReturn(buyer);
            when(buyer.getUserName()).thenReturn("buyer1");
            when(userDetails.getUsername()).thenReturn("buyer1");
            when(order.getStatus()).thenReturn(OrderStatus.RECEIVED);
            when(reviewRepository.existsByOrderId(orderId)).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> reviewService.createReview(userDetails, reviewReq));

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Valid request -> review is built and saved correctly")
        void success_saves_review() {
            when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
            when(order.getBuyer()).thenReturn(buyer);
            when(buyer.getUserName()).thenReturn("buyer1");
            when(userDetails.getUsername()).thenReturn("buyer1");
            when(order.getStatus()).thenReturn(OrderStatus.RECEIVED);
            when(reviewRepository.existsByOrderId(orderId)).thenReturn(false);

            reviewService.createReview(userDetails, reviewReq);

            ArgumentCaptor<ProductReview> captor = ArgumentCaptor.forClass(ProductReview.class);
            verify(reviewRepository, times(1)).save(captor.capture());

            ProductReview saved = captor.getValue();
            assertThat(saved.getOrder()).isEqualTo(order);
            assertThat(saved.getReviewer()).isEqualTo(buyer);
            assertThat(saved.getRating()).isEqualTo(reviewReq.getRating());
            assertThat(saved.getComment()).isEqualTo(reviewReq.getComment());
        }
    }

    // ==================== getReviewsForSeller ====================

    @Nested
    @DisplayName("getReviewsForSeller")
    class GetReviewsForSeller {

        @Test
        @DisplayName("No reviews found -> returns empty list")
        void no_reviews_returns_empty() {
            when(userDetails.getUsername()).thenReturn("seller1");
            when(reviewRepository.findBySellerUsername("seller1"))
                    .thenReturn(Collections.emptyList());

            List<ReviewRes> result = reviewService.getReviewsForSeller(userDetails);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Review with populated order items and reviewer -> maps all fields")
        void full_fields_mapped() {
            when(userDetails.getUsername()).thenReturn("seller1");

            ProductReview review = mock(ProductReview.class);
            Order reviewOrder = mock(Order.class);
            OrderItem orderItem = mock(OrderItem.class);
            Product product = mock(Product.class);
            User reviewer = mock(User.class);

            UUID reviewId = UUID.randomUUID();
            UUID reviewOrderId = UUID.randomUUID();
            UUID reviewerId = UUID.randomUUID();
            OffsetDateTime createdAt = OffsetDateTime.now();
            Short rating = (short) 4;

            when(review.getId()).thenReturn(reviewId);
            when(review.getOrder()).thenReturn(reviewOrder);
            when(reviewOrder.getId()).thenReturn(reviewOrderId);
            when(reviewOrder.getOrderItems()).thenReturn(List.of(orderItem));
            when(orderItem.getProduct()).thenReturn(product);
            when(product.getTitle()).thenReturn("Cool Product");
            when(review.getReviewer()).thenReturn(reviewer);
            when(reviewer.getUserId()).thenReturn(reviewerId);
            when(reviewer.getUserName()).thenReturn("buyerX");
            when(review.getRating()).thenReturn(rating);
            when(review.getComment()).thenReturn("Nice!");
            when(review.getCreatedAt()).thenReturn(createdAt);

            when(reviewRepository.findBySellerUsername("seller1"))
                    .thenReturn(List.of(review));

            List<ReviewRes> result = reviewService.getReviewsForSeller(userDetails);

            assertThat(result).hasSize(1);
            ReviewRes res = result.get(0);
            assertThat(res.getId()).isEqualTo(reviewId);
            assertThat(res.getOrderId()).isEqualTo(reviewOrderId);
            assertThat(res.getProductTitle()).isEqualTo("Cool Product");
            assertThat(res.getReviewerId()).isEqualTo(reviewerId);
            assertThat(res.getReviewerName()).isEqualTo("buyerX");
            assertThat(res.getRating()).isEqualTo(rating);
            assertThat(res.getComment()).isEqualTo("Nice!");
            assertThat(res.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Order items list is null -> productTitle is null")
        void null_order_items_gives_null_title() {
            when(userDetails.getUsername()).thenReturn("seller1");

            ProductReview review = mock(ProductReview.class);
            Order reviewOrder = mock(Order.class);
            User reviewer = mock(User.class);

            when(review.getOrder()).thenReturn(reviewOrder);
            when(reviewOrder.getOrderItems()).thenReturn(null);
            when(review.getReviewer()).thenReturn(reviewer);
            lenient().when(reviewer.getUserId()).thenReturn(UUID.randomUUID());
            lenient().when(reviewer.getUserName()).thenReturn("buyerY");

            when(reviewRepository.findBySellerUsername("seller1"))
                    .thenReturn(List.of(review));

            List<ReviewRes> result = reviewService.getReviewsForSeller(userDetails);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProductTitle()).isNull();
        }

        @Test
        @DisplayName("Order items list is empty -> productTitle is null")
        void empty_order_items_gives_null_title() {
            when(userDetails.getUsername()).thenReturn("seller1");

            ProductReview review = mock(ProductReview.class);
            Order reviewOrder = mock(Order.class);
            User reviewer = mock(User.class);

            when(review.getOrder()).thenReturn(reviewOrder);
            when(reviewOrder.getOrderItems()).thenReturn(Collections.emptyList());
            when(review.getReviewer()).thenReturn(reviewer);
            lenient().when(reviewer.getUserId()).thenReturn(UUID.randomUUID());
            lenient().when(reviewer.getUserName()).thenReturn("buyerZ");

            when(reviewRepository.findBySellerUsername("seller1"))
                    .thenReturn(List.of(review));

            List<ReviewRes> result = reviewService.getReviewsForSeller(userDetails);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProductTitle()).isNull();
        }

        @Test
        @DisplayName("Reviewer is null -> reviewerId and reviewerName are null")
        void null_reviewer_gives_null_fields() {
            when(userDetails.getUsername()).thenReturn("seller1");

            ProductReview review = mock(ProductReview.class);
            Order reviewOrder = mock(Order.class);

            when(review.getOrder()).thenReturn(reviewOrder);
            when(reviewOrder.getOrderItems()).thenReturn(Collections.emptyList());
            when(review.getReviewer()).thenReturn(null);

            when(reviewRepository.findBySellerUsername("seller1"))
                    .thenReturn(List.of(review));

            List<ReviewRes> result = reviewService.getReviewsForSeller(userDetails);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReviewerId()).isNull();
            assertThat(result.get(0).getReviewerName()).isNull();
        }
    }
}
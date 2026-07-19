package com.example.PRM.serviceImpl;

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
import com.example.PRM.service.ReviewService;
import com.example.PRM.status_enum.OrderStatus; // Hãy chắc chắn bạn import đúng enum của bạn
import lombok.RequiredArgsConstructor;
import com.example.PRM.dto.response.review.ReviewRes;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public void createReview(UserDetails userDetails, ReviewReq req) {

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order không tồn tại"));


        if (!order.getBuyer().getUserName().equals(userDetails.getUsername())) {
            throw new ForbiddenException("Chỉ người mua mới được phép đánh giá đơn hàng này");
        }


        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new BadRequestException("Đơn hàng chưa hoàn thành, không thể đánh giá");
        }

        if (reviewRepository.existsByOrderId(req.getOrderId())) {
            throw new BadRequestException("Bạn đã đánh giá đơn hàng này rồi");
        }

        ProductReview review = new ProductReview();
        review.setOrder(order);
        review.setReviewer(order.getBuyer());
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        reviewRepository.save(review);
    }

    @Override
    public List<ReviewRes> getReviewsForSeller(UserDetails userDetails) {
        String sellerUsername = userDetails.getUsername();
        List<ProductReview> reviews = reviewRepository.findBySellerUsername(sellerUsername);
        
        return reviews.stream().map(review -> ReviewRes.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .reviewerId(review.getReviewer() != null ? review.getReviewer().getUserId() : null)
                .reviewerName(review.getReviewer() != null ? review.getReviewer().getUserName() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }
}

package com.example.PRM.controller;

import com.example.PRM.dto.request.ReviewReq;
import com.example.PRM.dto.response.ApiResponse;
import com.example.PRM.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody ReviewReq req) {
        reviewService.createReview(userDetails, req);
        return ResponseEntity.ok(new ApiResponse(200, "Cảm ơn bạn đã đánh giá đơn hàng!"));
    }
}

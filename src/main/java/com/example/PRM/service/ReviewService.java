package com.example.PRM.service;
import com.example.PRM.dto.request.review.ReviewReq;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import com.example.PRM.dto.response.review.ReviewRes;

public interface ReviewService {
    void createReview(UserDetails userDetails, ReviewReq req);
    List<ReviewRes> getReviewsForSeller(UserDetails userDetails);
}
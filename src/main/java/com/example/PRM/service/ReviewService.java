package com.example.PRM.service;
import com.example.PRM.dto.request.ReviewReq;
import org.springframework.security.core.userdetails.UserDetails;

public interface ReviewService {
    void createReview(UserDetails userDetails, ReviewReq req);
}
package com.example.PRM.service;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface OutfitService {
    ResponseEntity<?> getOutfits(
            Authentication authentication,
            String message,
            int maxOutfits
    );
}
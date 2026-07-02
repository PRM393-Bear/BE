package com.example.PRM.service;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface OutfitService {
    ResponseEntity<?> getOutfits(Authentication authentication, int maxOutfits);
    ResponseEntity<?> getOutfitsByOccasion(Authentication authentication, Map<String, String> body, int maxOutfits);
}
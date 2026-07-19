package com.example.PRM.controller;

import com.example.PRM.dto.request.outfit.OutfitRequest;
import com.example.PRM.service.OutfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @PostMapping
    public ResponseEntity<?> getOutfits(
            Authentication authentication,
            @RequestBody OutfitRequest body,
            @RequestParam(defaultValue = "3") int maxOutfits
    ) {
        return outfitService.getOutfits(authentication, body.getMessage(), maxOutfits);
    }
}
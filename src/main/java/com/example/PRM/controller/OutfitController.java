package com.example.PRM.controller;

import com.example.PRM.service.OutfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/outfit")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @GetMapping
    public ResponseEntity<?> getOutfits(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int maxOutfits
    ) {
        return outfitService.getOutfits(authentication, maxOutfits);
    }

    @PostMapping("/occasion")
    public ResponseEntity<?> getOutfitsByOccasion(
            Authentication authentication,
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "3") int maxOutfits
    ) {
        return outfitService.getOutfitsByOccasion(authentication, body, maxOutfits);
    }
}
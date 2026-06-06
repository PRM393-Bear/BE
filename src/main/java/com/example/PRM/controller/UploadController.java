package com.example.PRM.controller;

import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadRes> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UploadRes result = uploadService.uploadImage(file, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
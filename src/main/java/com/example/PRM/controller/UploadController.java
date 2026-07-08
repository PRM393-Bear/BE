package com.example.PRM.controller;

import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.entity.User;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.service.AuditLogService;
import com.example.PRM.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadRes> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByUserName(username).orElseThrow(() -> new NotFoundException("User not found"));
        auditLogService.log("UPLOAD_IMAGE",
                "IMAGE",
                null,
                "User upload image successfully",
                "SUCCESS",
                user.getUserId(),
                user.getUserName(),
                request
        );

        UploadRes result = uploadService.uploadImage(file, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
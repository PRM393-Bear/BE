package com.example.PRM.service;

import com.example.PRM.dto.response.UploadRes;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadRes uploadImage(MultipartFile file, String username);
}
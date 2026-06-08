package com.example.PRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadRes {
    private String url;        // secure_url từ Cloudinary
    private String publicId;   // để xóa ảnh sau này (optional)
}
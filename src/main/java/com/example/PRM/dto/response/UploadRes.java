package com.example.PRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Data
@NoArgsConstructor
public class UploadRes {
    private String url;        // secure_url từ Cloudinary
    private String publicId;   // để xóa ảnh sau này (optional)
}
package com.example.PRM.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.PRM.config.CloudinaryProperties;
import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/pjpeg", "image/png", "image/webp", "image/gif"
    );

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public UploadRes uploadImage(MultipartFile file, String username) {
        validateFile(file);

        try {
            String folder = properties.getFolder() + "/" + username;

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            return new UploadRes(url, publicId);

        } catch (IOException e) {
            throw new BadRequestException("Không đọc được file upload");
        } catch (Exception e) {
            throw new BadRequestException("Upload Cloudinary thất bại: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File ảnh không được để trống");
        }

        if (!isAllowedImage(file)) {
            throw new BadRequestException(
                    "Chỉ chấp nhận ảnh JPEG, PNG, WEBP, GIF (nhận được: "
                            + file.getContentType() + ", file: " + file.getOriginalFilename() + ")"
            );
        }

        // 5MB — khớp application.yaml
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Ảnh không được vượt quá 5MB");
        }
    }

    private boolean isAllowedImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }

        String name = file.getOriginalFilename();
        if (name == null) {
            return false;
        }

        String lower = name.toLowerCase();
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp")
                || lower.endsWith(".gif");
    }
}
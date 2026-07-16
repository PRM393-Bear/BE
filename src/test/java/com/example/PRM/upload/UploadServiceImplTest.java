package com.example.PRM.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.PRM.config.CloudinaryProperties;
import com.example.PRM.dto.response.UploadRes;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.AuditLogServiceImpl;
import com.example.PRM.serviceImpl.UploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UploadServiceImpl}.
 * Covers validateFile branches, isAllowedImage branches, and all three
 * outcomes of uploadImage (success, IOException, generic Exception).
 */
@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private CloudinaryProperties properties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogServiceImpl auditLogService;

    @Mock
    private MultipartFile file;

    private UploadServiceImpl uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadServiceImpl(cloudinary, properties, userRepository, auditLogService);
    }

    // ---------------------------------------------------------------
    // validateFile branches
    // ---------------------------------------------------------------

    @Test
    void uploadImage_fileIsNull_throwsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> uploadService.uploadImage(null, "john.doe"));
        assertTrue(ex.getMessage().contains("không được để trống"));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void uploadImage_fileIsEmpty_throwsBadRequestException() {
        when(file.isEmpty()).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> uploadService.uploadImage(file, "john.doe"));
        assertTrue(ex.getMessage().contains("không được để trống"));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void uploadImage_disallowedTypeAndExtension_throwsBadRequestException() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> uploadService.uploadImage(file, "john.doe"));
        assertTrue(ex.getMessage().contains("Chỉ chấp nhận ảnh"));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void uploadImage_fileTooLarge_throwsBadRequestException() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(6L * 1024 * 1024);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> uploadService.uploadImage(file, "john.doe"));
        assertTrue(ex.getMessage().contains("5MB"));
        verifyNoInteractions(cloudinary);
    }

    // ---------------------------------------------------------------
    // isAllowedImage branches (exercised indirectly through uploadImage success)
    // ---------------------------------------------------------------

    @Test
    void uploadImage_allowedContentTypeUppercase_isAccepted() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("IMAGE/PNG");
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(properties.getFolder()).thenReturn("wardrobe");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenReturn(Map.of(
                "secure_url", "http://cloudinary.com/img.png",
                "public_id", "wardrobe/img"
        ));

        UploadRes result = uploadService.uploadImage(file, "john.doe");

        assertEquals("http://cloudinary.com/img.png", result.getUrl());
        assertEquals("wardrobe/img", result.getPublicId());
    }

    @Test
    void uploadImage_nullContentTypeButAllowedExtension_isAccepted() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);
        when(file.getOriginalFilename()).thenReturn("photo.JPEG");
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(properties.getFolder()).thenReturn("wardrobe");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenReturn(Map.of(
                "secure_url", "http://cloudinary.com/photo.jpeg",
                "public_id", "wardrobe/photo"
        ));

        UploadRes result = uploadService.uploadImage(file, "john.doe");

        assertEquals("http://cloudinary.com/photo.jpeg", result.getUrl());
    }

    @Test
    void uploadImage_nullContentTypeAndNullFilename_isRejected() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);
        when(file.getOriginalFilename()).thenReturn(null);

        assertThrows(BadRequestException.class, () -> uploadService.uploadImage(file, "john.doe"));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void uploadImage_invalidContentTypeButDisallowedExtension_isRejected() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/octet-stream");
        when(file.getOriginalFilename()).thenReturn("archive.zip");

        assertThrows(BadRequestException.class, () -> uploadService.uploadImage(file, "john.doe"));
        verifyNoInteractions(cloudinary);
    }

    // ---------------------------------------------------------------
    // uploadImage success / exception branches
    // ---------------------------------------------------------------

    @Test
    void uploadImage_success_returnsUploadRes() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(2048L);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3, 4});
        when(properties.getFolder()).thenReturn("wardrobe");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenReturn(Map.of(
                "secure_url", "http://cloudinary.com/shirt.jpg",
                "public_id", "wardrobe/john.doe/shirt"
        ));

        UploadRes result = uploadService.uploadImage(file, "john.doe");

        assertEquals("http://cloudinary.com/shirt.jpg", result.getUrl());
        assertEquals("wardrobe/john.doe/shirt", result.getPublicId());
    }

    @Test
    void uploadImage_getBytesThrowsIOException_throwsBadRequestException() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenThrow(new IOException("disk read failed"));
        when(properties.getFolder()).thenReturn("wardrobe");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> uploadService.uploadImage(file, "john.doe"));
        assertTrue(ex.getMessage().contains("Không đọc được file upload"));
    }

    @Test
    void uploadImage_cloudinaryThrowsGenericException_throwsBadRequestException() throws IOException {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(properties.getFolder()).thenReturn("wardrobe");
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any())).thenThrow(new RuntimeException("Cloudinary is down"));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> uploadService.uploadImage(file, "john.doe"));
        assertTrue(ex.getMessage().contains("Upload Cloudinary thất bại"));
        assertTrue(ex.getMessage().contains("Cloudinary is down"));
    }
}

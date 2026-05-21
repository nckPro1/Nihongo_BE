package org.example.nihongobackend.service.media;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.nihongobackend.dto.response.media.MediaUploadResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Upload thật lên Cloudinary. Chạy từ thư mục nihongo-backend (có file .env với CLOUDINARY_*).
 * <pre>
 *   mvn test -Dtest=CloudinaryUploadIntegrationTest
 * </pre>
 */
@SpringBootTest
class CloudinaryUploadIntegrationTest {

    private static final byte[] TINY_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    );

    @DynamicPropertySource
    static void loadCloudinaryFromEnvFile(DynamicPropertyRegistry registry) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        registry.add("cloudinary.enabled", () -> dotenv.get("CLOUDINARY_ENABLED", "false"));
        registry.add("cloudinary.cloud-name", () -> dotenv.get("CLOUDINARY_CLOUD_NAME", ""));
        registry.add("cloudinary.api-key", () -> dotenv.get("CLOUDINARY_API_KEY", ""));
        registry.add("cloudinary.api-secret", () -> dotenv.get("CLOUDINARY_API_SECRET", ""));
        registry.add("cloudinary.folder", () -> dotenv.get("CLOUDINARY_FOLDER", "zenigo"));
    }

    @Autowired(required = false)
    private CloudinaryMediaService cloudinaryMediaService;

    @Test
    void uploadsOnePixelPng_andReturnsSecureUrl() {
        Assumptions.assumeTrue(cloudinaryMediaService != null, "CloudinaryMediaService missing (cloudinary.enabled=false?)");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "smoke.png",
                "image/png",
                TINY_PNG
        );

        MediaUploadResponse response = cloudinaryMediaService.uploadImage(file, "smoke-test");

        assertNotNull(response.getSecureUrl());
        assertFalse(response.getSecureUrl().isBlank());
        assertTrue(response.getSecureUrl().contains("cloudinary.com"), "Expected Cloudinary URL");
        assertNotNull(response.getPublicId());
    }
}

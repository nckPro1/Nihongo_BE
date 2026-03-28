package org.example.nihongobackend.service.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.nihongobackend.config.CloudinaryProperties;
import org.example.nihongobackend.dto.response.media.MediaUploadResponse;
import org.example.nihongobackend.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryMediaService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryMediaService.class);

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryMediaService(
            @Autowired(required = false) Cloudinary cloudinary,
            CloudinaryProperties properties
    ) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    private void ensureConfigured() {
        if (!properties.isEnabled() || cloudinary == null) {
            throw new BadRequestException(
                    "Cloudinary is not configured. Set cloudinary.enabled=true and CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET."
            );
        }
    }

    /**
     * Upload ảnh hoặc video; resource_type tự nhận từ MIME (image/* → image, video/* → video).
     */
    public MediaUploadResponse upload(MultipartFile file, String subfolder) {
        ensureConfigured();
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new BadRequestException("Could not determine file type");
        }
        String resourceType = contentType.startsWith("video/") ? "video" : "image";
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new BadRequestException("Only image or video uploads are allowed");
        }
        return uploadRaw(file, subfolder, resourceType);
    }

    /**
     * Chỉ ảnh (avatar, thumbnail, …).
     */
    public MediaUploadResponse uploadImage(MultipartFile file, String subfolder) {
        ensureConfigured();
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        String contentType = file.getContentType();
        if (!isLikelyImage(contentType, file.getOriginalFilename())) {
            throw new BadRequestException("Only image files are allowed");
        }
        return uploadRaw(file, subfolder, "image");
    }

    private boolean isLikelyImage(String contentType, String originalFilename) {
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }
        if (originalFilename == null) {
            return false;
        }
        String lower = originalFilename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")
                || lower.endsWith(".heic") || lower.endsWith(".heif");
    }

    @SuppressWarnings("unchecked")
    private MediaUploadResponse uploadRaw(MultipartFile file, String subfolder, String resourceType) {
        String folder = properties.getFolder();
        if (subfolder != null && !subfolder.isBlank()) {
            folder = folder + "/" + subfolder.replaceAll("^/+|/+$", "");
        }
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", resourceType
                    )
            );
            return mapResponse(uploadResult);
        } catch (IOException e) {
            log.warn("Read multipart file failed: {}", e.getMessage());
            throw new BadRequestException("Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Cloudinary upload failed", e);
            throw new BadRequestException("Upload failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private MediaUploadResponse mapResponse(Map<String, Object> uploadResult) {
        MediaUploadResponse r = new MediaUploadResponse();
        r.setPublicId((String) uploadResult.get("public_id"));
        r.setSecureUrl((String) uploadResult.get("secure_url"));
        r.setResourceType((String) uploadResult.get("resource_type"));
        r.setFormat((String) uploadResult.get("format"));
        Object w = uploadResult.get("width");
        if (w instanceof Number) {
            r.setWidth(((Number) w).intValue());
        }
        Object h = uploadResult.get("height");
        if (h instanceof Number) {
            r.setHeight(((Number) h).intValue());
        }
        Object d = uploadResult.get("duration");
        if (d instanceof Number) {
            r.setDuration(((Number) d).doubleValue());
        }
        return r;
    }
}

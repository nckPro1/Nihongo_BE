package org.example.nihongobackend.controller.media;

import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.media.MediaUploadResponse;
import org.example.nihongobackend.service.media.CloudinaryMediaService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload ảnh/video chung (flashcard, bài học, …). Yêu cầu JWT.
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryMediaService cloudinaryMediaService;

    public MediaController(CloudinaryMediaService cloudinaryMediaService) {
        this.cloudinaryMediaService = cloudinaryMediaService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder
    ) {
        MediaUploadResponse data = cloudinaryMediaService.upload(file, folder);
        return ApiResponse.success("Upload successful", data);
    }
}

package org.example.nihongobackend.service.media;

import org.example.nihongobackend.dto.response.media.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryMediaService {

    MediaUploadResponse upload(MultipartFile file, String subfolder);

    MediaUploadResponse uploadImage(MultipartFile file, String subfolder);
}

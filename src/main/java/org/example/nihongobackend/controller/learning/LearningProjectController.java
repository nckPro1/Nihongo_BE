package org.example.nihongobackend.controller.learning;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.learning.CreateLearningProjectRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.learning.LearningProjectResponse;
import org.example.nihongobackend.service.learning.LearningProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learning-projects")
public class LearningProjectController {

    private final LearningProjectService learningProjectService;

    public LearningProjectController(LearningProjectService learningProjectService) {
        this.learningProjectService = learningProjectService;
    }

    @GetMapping
    public ApiResponse<List<LearningProjectResponse>> list() {
        return ApiResponse.success("OK", learningProjectService.listMine());
    }

    @PostMapping
    public ApiResponse<LearningProjectResponse> create(@Valid @RequestBody CreateLearningProjectRequest request) {
        LearningProjectResponse data = learningProjectService.create(request);
        return ApiResponse.success("Đã tạo dự án học tập", data);
    }
}

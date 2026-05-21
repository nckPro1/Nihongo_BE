package org.example.nihongobackend.controller.learning;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.learning.CreateLearningProjectRequest;
import org.example.nihongobackend.dto.request.learning.SubmitQuizResultsRequest;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.learning.LearningProjectResponse;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.learning.FlashcardQuizProgressService;
import org.example.nihongobackend.service.learning.LearningProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/learning-projects")
public class LearningProjectController {

    private final LearningProjectService learningProjectService;
    private final FlashcardQuizProgressService flashcardQuizProgressService;
    private final UserRepository userRepository;

    public LearningProjectController(
            LearningProjectService learningProjectService,
            FlashcardQuizProgressService flashcardQuizProgressService,
            UserRepository userRepository) {
        this.learningProjectService = learningProjectService;
        this.flashcardQuizProgressService = flashcardQuizProgressService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<LearningProjectResponse>> list() {
        return ApiResponse.success("OK", learningProjectService.listMine());
    }

    @PostMapping
    public ApiResponse<LearningProjectResponse> create(@Valid @RequestBody CreateLearningProjectRequest request) {
        LearningProjectResponse data = learningProjectService.create(request);
        return ApiResponse.success("Đã tạo Zenigo", data);
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> delete(@PathVariable UUID projectId) {
        learningProjectService.delete(projectId);
        return ApiResponse.success("Đã xoá Zenigo và toàn bộ thẻ", null);
    }

    /**
     * Gửi kết quả từng câu sau phiên quiz để cập nhật tiến độ nắm từ (lưu server, tính % lộ trình bộ thẻ).
     */
    @PostMapping("/{projectId}/quiz-results")
    public ApiResponse<Void> submitQuizResults(
            @PathVariable UUID projectId,
            @Valid @RequestBody SubmitQuizResultsRequest body) {
        User user = loadCurrentUser();
        flashcardQuizProgressService.submitQuizResults(user, projectId, body.getResults());
        return ApiResponse.success("Đã cập nhật tiến độ quiz", null);
    }

    private User loadCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}

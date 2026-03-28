package org.example.nihongobackend.service.learning;

import org.example.nihongobackend.dto.request.learning.CreateLearningProjectRequest;
import org.example.nihongobackend.dto.response.learning.LearningProjectResponse;
import org.example.nihongobackend.entity.LearningProject;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.FlashcardRepository;
import org.example.nihongobackend.repository.LearningProjectRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearningProjectService {

    private static final String DEFAULT_PROJECT_NAME = "Kho mặc định";

    private final LearningProjectRepository learningProjectRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    public LearningProjectService(
            LearningProjectRepository learningProjectRepository,
            FlashcardRepository flashcardRepository,
            UserRepository userRepository) {
        this.learningProjectRepository = learningProjectRepository;
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Danh sách dự án của user; nếu chưa có dự án nào thì tạo "Kho mặc định".
     */
    @Transactional
    public List<LearningProjectResponse> listMine() {
        User user = loadCurrentUser();
        List<LearningProject> list = learningProjectRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        if (list.isEmpty()) {
            LearningProject p = new LearningProject();
            p.setUser(user);
            p.setName(DEFAULT_PROJECT_NAME);
            learningProjectRepository.save(p);
            list = learningProjectRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public LearningProjectResponse create(CreateLearningProjectRequest request) {
        User user = loadCurrentUser();
        String name = request.getName().trim();
        if (name.isEmpty()) {
            throw new BadRequestException("Tên dự án không được để trống");
        }
        LearningProject p = new LearningProject();
        p.setUser(user);
        p.setName(name);
        p.setDescription(blankToNull(request.getDescription()));
        LearningProject saved = learningProjectRepository.save(p);
        return toResponse(saved);
    }

    public LearningProject requireOwnedProject(User user, UUID projectId) {
        LearningProject p = learningProjectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy dự án học tập"));
        if (!p.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền truy cập dự án này");
        }
        return p;
    }

    private LearningProjectResponse toResponse(LearningProject p) {
        LearningProjectResponse r = new LearningProjectResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setCreatedAt(p.getCreatedAt());
        r.setCardCount(flashcardRepository.countByProject_Id(p.getId()));
        return r;
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
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

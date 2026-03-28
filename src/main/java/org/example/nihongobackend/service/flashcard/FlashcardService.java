package org.example.nihongobackend.service.flashcard;

import org.example.nihongobackend.dto.request.flashcard.CreateFlashcardRequest;
import org.example.nihongobackend.dto.response.flashcard.FlashcardResponse;
import org.example.nihongobackend.entity.Flashcard;
import org.example.nihongobackend.entity.LearningProject;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.FlashcardRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.learning.LearningProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final LearningProjectService learningProjectService;

    public FlashcardService(
            FlashcardRepository flashcardRepository,
            UserRepository userRepository,
            LearningProjectService learningProjectService) {
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
        this.learningProjectService = learningProjectService;
    }

    public List<FlashcardResponse> listByProject(UUID projectId) {
        User user = loadCurrentUser();
        learningProjectService.requireOwnedProject(user, projectId);
        return flashcardRepository.findByProject_IdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FlashcardResponse create(CreateFlashcardRequest request) {
        User user = loadCurrentUser();
        LearningProject project = learningProjectService.requireOwnedProject(user, request.getProjectId());

        String kanji = request.getKanji().trim();
        String meaning = request.getMeaning().trim();
        if (kanji.isEmpty() || meaning.isEmpty()) {
            throw new BadRequestException("Kanji và nghĩa không được để trống");
        }
        if (flashcardRepository.existsByProject_IdAndKanjiAndMeaning(project.getId(), kanji, meaning)) {
            throw new BadRequestException("Thẻ này đã có trong dự án này");
        }

        Flashcard card = new Flashcard();
        card.setUser(user);
        card.setProject(project);
        card.setKanji(kanji);
        card.setReading(blankToNull(request.getReading()));
        card.setMeaning(meaning);
        card.setDirection(blankToNull(request.getDirection()));
        card.setSourceQuery(blankToNull(request.getSourceQuery()));

        Flashcard saved = flashcardRepository.save(card);
        return toResponse(saved);
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private FlashcardResponse toResponse(Flashcard c) {
        FlashcardResponse r = new FlashcardResponse();
        r.setId(c.getId());
        if (c.getProject() != null) {
            r.setProjectId(c.getProject().getId());
        }
        r.setKanji(c.getKanji());
        r.setReading(c.getReading());
        r.setMeaning(c.getMeaning());
        r.setDirection(c.getDirection());
        r.setSourceQuery(c.getSourceQuery());
        r.setCreatedAt(c.getCreatedAt());
        return r;
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

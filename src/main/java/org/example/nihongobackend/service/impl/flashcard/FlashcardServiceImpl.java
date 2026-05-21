package org.example.nihongobackend.service.impl.flashcard;

import org.example.nihongobackend.dto.request.flashcard.BulkCreateFlashcardsRequest;
import org.example.nihongobackend.dto.request.flashcard.BulkFlashcardItemRequest;
import org.example.nihongobackend.dto.request.flashcard.CreateFlashcardRequest;
import org.example.nihongobackend.dto.response.flashcard.BulkCreateFlashcardsResponse;
import org.example.nihongobackend.dto.response.flashcard.FlashcardResponse;
import org.example.nihongobackend.entity.Flashcard;
import org.example.nihongobackend.entity.LearningProject;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.FlashcardRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.flashcard.FlashcardService;
import org.example.nihongobackend.service.learning.FlashcardQuizProgressService;
import org.example.nihongobackend.service.learning.LearningProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final LearningProjectService learningProjectService;
    private final FlashcardQuizProgressService flashcardQuizProgressService;

    public FlashcardServiceImpl(
            FlashcardRepository flashcardRepository,
            UserRepository userRepository,
            LearningProjectService learningProjectService,
            FlashcardQuizProgressService flashcardQuizProgressService) {
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
        this.learningProjectService = learningProjectService;
        this.flashcardQuizProgressService = flashcardQuizProgressService;
    }

    @Override
    public List<FlashcardResponse> listByProject(UUID projectId) {
        User user = loadCurrentUser();
        learningProjectService.requireOwnedProject(user, projectId);
        List<Flashcard> cards = flashcardRepository.findByProject_IdOrderByCreatedAtDesc(projectId);
        List<UUID> ids = cards.stream().map(Flashcard::getId).toList();
        Map<UUID, Integer> mastery = flashcardQuizProgressService.masteryByFlashcardIds(user, ids);
        return cards.stream()
                .map(c -> toResponse(c, mastery.getOrDefault(c.getId(), 0)))
                .collect(Collectors.toList());
    }

    @Override
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
            throw new BadRequestException("Thẻ này đã có trong Zenigo này");
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
        return toResponse(saved, 0);
    }

    @Override
    @Transactional
    public BulkCreateFlashcardsResponse createBatch(BulkCreateFlashcardsRequest request) {
        User user = loadCurrentUser();
        LearningProject project = learningProjectService.requireOwnedProject(user, request.getProjectId());
        UUID projectId = project.getId();

        int skippedInvalid = 0;
        int skippedDuplicates = 0;
        Set<String> seenInBatch = new HashSet<>();
        List<Flashcard> toSave = new ArrayList<>();

        for (BulkFlashcardItemRequest item : request.getItems()) {
            String kanji = item.getKanji() == null ? "" : item.getKanji().trim();
            String meaning = item.getMeaning() == null ? "" : item.getMeaning().trim();
            if (kanji.isEmpty() || meaning.isEmpty()) {
                skippedInvalid++;
                continue;
            }
            String batchKey = kanji + "\u0000" + meaning;
            if (seenInBatch.contains(batchKey)) {
                skippedDuplicates++;
                continue;
            }
            if (flashcardRepository.existsByProject_IdAndKanjiAndMeaning(projectId, kanji, meaning)) {
                skippedDuplicates++;
                continue;
            }
            seenInBatch.add(batchKey);

            Flashcard card = new Flashcard();
            card.setUser(user);
            card.setProject(project);
            card.setKanji(kanji);
            card.setReading(blankToNull(item.getReading()));
            card.setMeaning(meaning);
            card.setDirection(null);
            card.setSourceQuery(null);
            toSave.add(card);
        }

        if (!toSave.isEmpty()) {
            flashcardRepository.saveAll(toSave);
        }

        BulkCreateFlashcardsResponse out = new BulkCreateFlashcardsResponse();
        out.setCreated(toSave.size());
        out.setSkippedDuplicates(skippedDuplicates);
        out.setSkippedInvalid(skippedInvalid);
        return out;
    }

    @Override
    @Transactional
    public void delete(UUID flashcardId) {
        User user = loadCurrentUser();
        Flashcard card = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thẻ"));
        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền xoá thẻ này");
        }
        flashcardQuizProgressService.deleteProgressByFlashcardId(flashcardId);
        flashcardRepository.delete(card);
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private FlashcardResponse toResponse(Flashcard c, int masteryScore) {
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
        r.setMasteryScore(masteryScore);
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

package org.example.nihongobackend.service.impl.learning;

import org.example.nihongobackend.dto.request.learning.QuizResultItemRequest;
import org.example.nihongobackend.entity.Flashcard;
import org.example.nihongobackend.entity.FlashcardQuizProgress;
import org.example.nihongobackend.entity.LearningProject;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.FlashcardQuizProgressRepository;
import org.example.nihongobackend.repository.FlashcardRepository;
import org.example.nihongobackend.repository.LearningProjectRepository;
import org.example.nihongobackend.service.learning.FlashcardQuizProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FlashcardQuizProgressServiceImpl implements FlashcardQuizProgressService {

    private static final int CORRECT_BUMP = 22;
    private static final int WRONG_PENALTY = 18;
    private static final long SUBMIT_DEDUP_WINDOW_MS = 4000;

    private final FlashcardQuizProgressRepository progressRepository;
    private final FlashcardRepository flashcardRepository;
    private final LearningProjectRepository learningProjectRepository;
    private final ConcurrentHashMap<String, Long> recentSubmitMillis = new ConcurrentHashMap<>();

    public FlashcardQuizProgressServiceImpl(
            FlashcardQuizProgressRepository progressRepository,
            FlashcardRepository flashcardRepository,
            LearningProjectRepository learningProjectRepository) {
        this.progressRepository = progressRepository;
        this.flashcardRepository = flashcardRepository;
        this.learningProjectRepository = learningProjectRepository;
    }

    @Override
    public ProjectDeckProgress computeDeckProgress(User user, UUID projectId) {
        LearningProject project = learningProjectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy Zenigo"));
        if (!project.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền truy cập Zenigo này");
        }

        List<Flashcard> cards = flashcardRepository.findByProject_IdOrderByCreatedAtDesc(projectId);
        long cardCount = cards.size();
        if (cardCount == 0) {
            return new ProjectDeckProgress(0, 0, 0);
        }

        List<UUID> ids = cards.stream().map(Flashcard::getId).toList();
        List<FlashcardQuizProgress> rows = progressRepository.findByUser_IdAndFlashcard_IdIn(user.getId(), ids);
        Map<UUID, Integer> scoreByCard = new HashMap<>();
        for (FlashcardQuizProgress row : rows) {
            scoreByCard.put(row.getFlashcard().getId(), clampMastery(row.getMasteryScore()));
        }

        long sum = 0;
        long mastered = 0;
        for (UUID id : ids) {
            int s = scoreByCard.getOrDefault(id, 0);
            sum += s;
            if (s >= FlashcardQuizProgressService.MASTERED_THRESHOLD) {
                mastered++;
            }
        }
        int progressPercent = (int) Math.round((double) sum / cardCount);
        return new ProjectDeckProgress(progressPercent, mastered, cardCount);
    }

    @Override
    public Map<UUID, Integer> masteryByFlashcardIds(User user, List<UUID> flashcardIds) {
        if (flashcardIds == null || flashcardIds.isEmpty()) {
            return Map.of();
        }
        List<FlashcardQuizProgress> rows = progressRepository.findByUser_IdAndFlashcard_IdIn(user.getId(), flashcardIds);
        Map<UUID, Integer> out = new HashMap<>();
        for (FlashcardQuizProgress row : rows) {
            out.put(row.getFlashcard().getId(), clampMastery(row.getMasteryScore()));
        }
        return out;
    }

    @Override
    @Transactional
    public void submitQuizResults(User user, UUID projectId, List<QuizResultItemRequest> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        String dedupKey = submitDedupKey(user.getId(), projectId, results);
        long now = System.currentTimeMillis();
        Long prev = recentSubmitMillis.put(dedupKey, now);
        if (prev != null && now - prev < SUBMIT_DEDUP_WINDOW_MS) {
            return;
        }
        pruneOldSubmits(now);
        LearningProject project = learningProjectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy Zenigo"));
        if (!project.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền truy cập Zenigo này");
        }

        for (QuizResultItemRequest line : results) {
            if (line.getFlashcardId() == null || line.getCorrect() == null) {
                continue;
            }
            Flashcard card = flashcardRepository.findById(line.getFlashcardId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy flashcard"));
            if (!card.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("Bạn không có quyền cập nhật thẻ này");
            }
            if (card.getProject() == null || !card.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Thẻ không thuộc Zenigo này");
            }

            FlashcardQuizProgress row = progressRepository
                    .findByUser_IdAndFlashcard_Id(user.getId(), card.getId())
                    .orElseGet(() -> {
                        FlashcardQuizProgress p = new FlashcardQuizProgress();
                        p.setUser(user);
                        p.setFlashcard(card);
                        return p;
                    });

            if (Boolean.TRUE.equals(line.getCorrect())) {
                row.setMasteryScore(Math.min(100, row.getMasteryScore() + CORRECT_BUMP));
                row.setCorrectCount(row.getCorrectCount() + 1);
            } else {
                row.setMasteryScore(Math.max(0, row.getMasteryScore() - WRONG_PENALTY));
                row.setWrongCount(row.getWrongCount() + 1);
            }
            progressRepository.save(row);
        }
    }

    private static int clampMastery(int s) {
        return Math.max(0, Math.min(100, s));
    }

    private void pruneOldSubmits(long now) {
        if (recentSubmitMillis.size() > 2000) {
            recentSubmitMillis.entrySet().removeIf(e -> now - e.getValue() > SUBMIT_DEDUP_WINDOW_MS * 4L);
        }
    }

    private static String submitDedupKey(UUID userId, UUID projectId, List<QuizResultItemRequest> results) {
        String payload = results.stream()
                .filter(r -> r.getFlashcardId() != null && r.getCorrect() != null)
                .sorted(Comparator.comparing(QuizResultItemRequest::getFlashcardId))
                .map(r -> r.getFlashcardId() + ":" + r.getCorrect())
                .collect(Collectors.joining(","));
        return userId + "|" + projectId + "|" + payload.hashCode();
    }

    @Override
    @Transactional
    public void deleteProgressByFlashcardId(UUID flashcardId) {
        progressRepository.deleteByFlashcard_Id(flashcardId);
    }

    @Override
    @Transactional
    public void deleteProgressByFlashcardIds(java.util.Collection<UUID> flashcardIds) {
        if (!flashcardIds.isEmpty()) {
            progressRepository.deleteByFlashcard_IdIn(flashcardIds);
        }
    }
}

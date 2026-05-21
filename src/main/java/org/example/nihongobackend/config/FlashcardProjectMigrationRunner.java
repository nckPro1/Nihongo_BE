package org.example.nihongobackend.config;

import org.example.nihongobackend.entity.Flashcard;
import org.example.nihongobackend.entity.LearningProject;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.repository.FlashcardRepository;
import org.example.nihongobackend.repository.LearningProjectRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gán flashcard cũ (chưa có project) vào một Zenigo "Kho mặc định" theo từng user.
 */
@Component
@Order(5)
public class FlashcardProjectMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlashcardProjectMigrationRunner.class);
    private static final String DEFAULT_NAME = "Kho mặc định";

    private final FlashcardRepository flashcardRepository;
    private final LearningProjectRepository learningProjectRepository;
    private final UserRepository userRepository;

    public FlashcardProjectMigrationRunner(
            FlashcardRepository flashcardRepository,
            LearningProjectRepository learningProjectRepository,
            UserRepository userRepository) {
        this.flashcardRepository = flashcardRepository;
        this.learningProjectRepository = learningProjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Flashcard> orphans = flashcardRepository.findByProjectIsNull();
        if (orphans.isEmpty()) {
            return;
        }
        Map<UUID, List<Flashcard>> byUser = orphans.stream()
                .collect(Collectors.groupingBy(f -> f.getUser().getId()));
        int fixed = 0;
        for (Map.Entry<UUID, List<Flashcard>> e : byUser.entrySet()) {
            User user = userRepository.findById(e.getKey()).orElse(null);
            if (user == null) {
                continue;
            }
            LearningProject project = learningProjectRepository
                    .findFirstByUser_IdOrderByCreatedAtAsc(user.getId())
                    .orElseGet(() -> {
                        LearningProject p = new LearningProject();
                        p.setUser(user);
                        p.setName(DEFAULT_NAME);
                        return learningProjectRepository.save(p);
                    });
            for (Flashcard f : e.getValue()) {
                f.setProject(project);
                fixed++;
            }
        }
        flashcardRepository.saveAll(orphans);
        if (fixed > 0) {
            log.info("Flashcard migration: gán project cho {} thẻ (project null).", fixed);
        }
    }
}

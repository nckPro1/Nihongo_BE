package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.LearningProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearningProjectRepository extends JpaRepository<LearningProject, UUID> {

    List<LearningProject> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<LearningProject> findFirstByUser_IdOrderByCreatedAtAsc(UUID userId);
}

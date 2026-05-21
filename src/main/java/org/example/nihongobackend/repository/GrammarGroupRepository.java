package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.GrammarGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GrammarGroupRepository extends JpaRepository<GrammarGroup, UUID> {

    List<GrammarGroup> findByJlptLevelOrderBySortOrderAsc(String jlptLevel);

    long countByJlptLevel(String jlptLevel);
}

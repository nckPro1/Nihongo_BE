package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.GrammarPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GrammarPointRepository extends JpaRepository<GrammarPoint, UUID> {

    @Query("SELECT p FROM GrammarPoint p JOIN FETCH p.group g WHERE g.jlptLevel = :level ORDER BY g.sortOrder ASC, p.sortOrder ASC")
    List<GrammarPoint> findByLevelWithGroup(@Param("level") String level);

    List<GrammarPoint> findByGroup_IdOrderBySortOrderAsc(UUID groupId);

    long countByGroup_JlptLevel(String jlptLevel);

    long countByGroup_Id(UUID groupId);
}

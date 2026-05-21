package org.example.nihongobackend.repository;

import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserFavourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFavouriteRepository extends JpaRepository<UserFavourite, UUID> {

    boolean existsByUserAndTargetTypeAndTargetId(User user, String targetType, UUID targetId);

    Optional<UserFavourite> findByUserAndTargetTypeAndTargetId(User user, String targetType, UUID targetId);

    void deleteByUserAndTargetTypeAndTargetId(User user, String targetType, UUID targetId);

    @Query("SELECT f.targetId FROM UserFavourite f WHERE f.user.id = :userId AND f.targetType = :tt AND f.targetId IN :ids")
    List<UUID> findTargetIdsIn(
            @Param("userId") UUID userId, @Param("tt") String targetType, @Param("ids") Collection<UUID> ids);

    @Query("SELECT f FROM UserFavourite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<UserFavourite> findMineOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT f FROM UserFavourite f WHERE f.user.id = :userId AND f.targetType = :targetType ORDER BY f.createdAt DESC")
    List<UserFavourite> findMineByTypeOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("targetType") String targetType);
}

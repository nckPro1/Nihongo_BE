package org.example.nihongobackend.service.favourite;

import org.example.nihongobackend.dto.response.favourite.FavouriteItemResponse;
import org.example.nihongobackend.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserFavouriteService {

    boolean isFavourite(User user, String targetType, UUID targetId);

    Set<UUID> favouriteTargetIdsIn(User user, String targetType, Collection<UUID> targetIds);

    void add(User user, String targetType, UUID targetId);

    void remove(User user, String targetType, UUID targetId);

    List<FavouriteItemResponse> listForUser(User user, String typeFilter);
}

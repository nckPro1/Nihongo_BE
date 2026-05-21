package org.example.nihongobackend.service.impl.favourite;

import org.example.nihongobackend.dto.response.favourite.FavouriteItemResponse;
import org.example.nihongobackend.entity.GrammarPoint;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserFavourite;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.repository.GrammarPointRepository;
import org.example.nihongobackend.repository.UserFavouriteRepository;
import org.example.nihongobackend.service.favourite.UserFavouriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserFavouriteServiceImpl implements UserFavouriteService {

    private final UserFavouriteRepository userFavouriteRepository;
    private final GrammarPointRepository grammarPointRepository;

    public UserFavouriteServiceImpl(
            UserFavouriteRepository userFavouriteRepository,
            GrammarPointRepository grammarPointRepository) {
        this.userFavouriteRepository = userFavouriteRepository;
        this.grammarPointRepository = grammarPointRepository;
    }

    @Override
    public boolean isFavourite(User user, String targetType, UUID targetId) {
        return userFavouriteRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId);
    }

    @Override
    public Set<UUID> favouriteTargetIdsIn(User user, String targetType, Collection<UUID> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(userFavouriteRepository.findTargetIdsIn(user.getId(), targetType, targetIds));
    }

    @Override
    @Transactional
    public void add(User user, String targetType, UUID targetId) {
        validateType(targetType);
        if (UserFavourite.TYPE_GRAMMAR.equals(targetType)) {
            if (!grammarPointRepository.existsById(targetId)) {
                throw new BadRequestException("Không tìm thấy mẫu ngữ pháp");
            }
        }
        if (userFavouriteRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId)) {
            return;
        }
        UserFavourite f = new UserFavourite();
        f.setUser(user);
        f.setTargetType(targetType);
        f.setTargetId(targetId);
        userFavouriteRepository.save(f);
    }

    @Override
    @Transactional
    public void remove(User user, String targetType, UUID targetId) {
        validateType(targetType);
        userFavouriteRepository.deleteByUserAndTargetTypeAndTargetId(user, targetType, targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavouriteItemResponse> listForUser(User user, String typeFilter) {
        List<UserFavourite> favs;
        if (typeFilter != null && !typeFilter.isBlank()) {
            String t = typeFilter.trim().toUpperCase();
            validateType(t);
            favs = userFavouriteRepository.findMineByTypeOrderByCreatedAtDesc(user.getId(), t);
        } else {
            favs = userFavouriteRepository.findMineOrderByCreatedAtDesc(user.getId());
        }

        List<UUID> grammarIds = favs.stream()
                .filter(f -> UserFavourite.TYPE_GRAMMAR.equals(f.getTargetType()))
                .map(UserFavourite::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, GrammarPoint> grammarById = grammarIds.isEmpty()
                ? Map.of()
                : grammarPointRepository.findAllById(grammarIds).stream()
                        .collect(Collectors.toMap(GrammarPoint::getId, g -> g, (a, b) -> a));

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<FavouriteItemResponse> out = new ArrayList<>();
        for (UserFavourite f : favs) {
            FavouriteItemResponse r = new FavouriteItemResponse();
            r.setTargetType(f.getTargetType());
            r.setTargetId(f.getTargetId());
            r.setCreatedAt(f.getCreatedAt() != null ? f.getCreatedAt().format(fmt) : "");
            if (UserFavourite.TYPE_GRAMMAR.equals(f.getTargetType())) {
                GrammarPoint p = grammarById.get(f.getTargetId());
                if (p != null) {
                    r.setTitle(p.getTitle());
                    r.setDetail(shortenMeaning(p.getMeaning(), 120));
                    var g = p.getGroup();
                    r.setGrammarJlptLevel(g != null ? g.getJlptLevel() : null);
                    r.setResourceMissing(false);
                } else {
                    r.setTitle("(Mẫu đã gỡ khỏi thư viện)");
                    r.setDetail(null);
                    r.setGrammarJlptLevel(null);
                    r.setResourceMissing(true);
                }
            } else {
                r.setTitle("Bài viết");
                r.setDetail("Sẽ hiển thị khi tính năng blog có dữ liệu.");
                r.setGrammarJlptLevel(null);
                r.setResourceMissing(false);
            }
            out.add(r);
        }
        return out;
    }

    private static String shortenMeaning(String meaning, int max) {
        if (meaning == null) {
            return "";
        }
        String t = meaning.replace('\n', ' ').trim();
        return t.length() <= max ? t : t.substring(0, max - 1) + "…";
    }

    private static void validateType(String targetType) {
        if (targetType == null
                || (!UserFavourite.TYPE_GRAMMAR.equals(targetType) && !UserFavourite.TYPE_BLOG.equals(targetType))) {
            throw new BadRequestException("targetType phải là GRAMMAR hoặc BLOG");
        }
    }
}

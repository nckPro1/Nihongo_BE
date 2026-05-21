package org.example.nihongobackend.service.impl.grammar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nihongobackend.dto.response.grammar.GrammarExampleResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarGroupSummaryResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarLevelSummaryResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarPointDetailResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarPointListItemResponse;
import org.example.nihongobackend.entity.GrammarGroup;
import org.example.nihongobackend.entity.GrammarPoint;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserFavourite;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.exception.NotFoundException;
import org.example.nihongobackend.exception.UnauthorizedException;
import org.example.nihongobackend.repository.GrammarGroupRepository;
import org.example.nihongobackend.repository.GrammarPointRepository;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.favourite.UserFavouriteService;
import org.example.nihongobackend.service.grammar.GrammarService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrammarServiceImpl implements GrammarService {

    private static final List<String> LEVEL_ORDER = List.of("N5", "N4", "N3", "N2", "N1");

    private final GrammarGroupRepository grammarGroupRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final UserRepository userRepository;
    private final UserFavouriteService userFavouriteService;
    private final ObjectMapper objectMapper;

    public GrammarServiceImpl(
            GrammarGroupRepository grammarGroupRepository,
            GrammarPointRepository grammarPointRepository,
            UserRepository userRepository,
            UserFavouriteService userFavouriteService,
            ObjectMapper objectMapper) {
        this.grammarGroupRepository = grammarGroupRepository;
        this.grammarPointRepository = grammarPointRepository;
        this.userRepository = userRepository;
        this.userFavouriteService = userFavouriteService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrammarLevelSummaryResponse> listLevels() {
        List<GrammarLevelSummaryResponse> out = new ArrayList<>();
        for (String level : LEVEL_ORDER) {
            GrammarLevelSummaryResponse r = new GrammarLevelSummaryResponse();
            r.setLevel(level);
            r.setGrammarCount(grammarPointRepository.countByGroup_JlptLevel(level));
            r.setGroupCount(grammarGroupRepository.countByJlptLevel(level));
            out.add(r);
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrammarGroupSummaryResponse> listGroups(String level) {
        String lv = normalizeLevel(level);
        return grammarGroupRepository.findByJlptLevelOrderBySortOrderAsc(lv).stream()
                .map(g -> {
                    GrammarGroupSummaryResponse r = new GrammarGroupSummaryResponse();
                    r.setId(g.getId());
                    r.setJlptLevel(g.getJlptLevel());
                    r.setName(g.getName());
                    r.setDescription(g.getDescription());
                    r.setSortOrder(g.getSortOrder() != null ? g.getSortOrder() : 0);
                    r.setPointCount(grammarPointRepository.countByGroup_Id(g.getId()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrammarPointListItemResponse> listPoints(String level, UUID groupId, String q) {
        String lv = normalizeLevel(level);
        User user = loadCurrentUser();
        String needle = q == null || q.isBlank() ? null : q.trim().toLowerCase(Locale.ROOT);

        List<GrammarPoint> raw = grammarPointRepository.findByLevelWithGroup(lv).stream()
                .filter(p -> groupId == null || p.getGroup().getId().equals(groupId))
                .filter(p -> needle == null
                        || p.getTitle().toLowerCase(Locale.ROOT).contains(needle)
                        || p.getMeaning().toLowerCase(Locale.ROOT).contains(needle))
                .collect(Collectors.toList());

        List<UUID> ids = raw.stream().map(GrammarPoint::getId).collect(Collectors.toList());
        Set<UUID> fav = userFavouriteService.favouriteTargetIdsIn(user, UserFavourite.TYPE_GRAMMAR, ids);

        Comparator<GrammarPoint> cmp = Comparator
                .comparing((GrammarPoint p) -> !fav.contains(p.getId()))
                .thenComparing(p -> p.getGroup().getSortOrder() != null ? p.getGroup().getSortOrder() : 0)
                .thenComparing(p -> p.getSortOrder() != null ? p.getSortOrder() : 0)
                .thenComparing(GrammarPoint::getTitle);

        return raw.stream()
                .sorted(cmp)
                .map(p -> toListItem(p, fav.contains(p.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GrammarPointDetailResponse getPointDetail(UUID pointId) {
        User user = loadCurrentUser();
        GrammarPoint p = grammarPointRepository.findById(pointId).orElseThrow(() ->
                new NotFoundException("Không tìm thấy mẫu ngữ pháp"));
        GrammarGroup g = p.getGroup();
        List<GrammarPoint> siblings = grammarPointRepository.findByGroup_IdOrderBySortOrderAsc(g.getId());
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(pointId)) {
                idx = i;
                break;
            }
        }
        UUID prev = idx > 0 ? siblings.get(idx - 1).getId() : null;
        UUID next = idx >= 0 && idx < siblings.size() - 1 ? siblings.get(idx + 1).getId() : null;

        boolean fav = userFavouriteService.isFavourite(user, UserFavourite.TYPE_GRAMMAR, pointId);
        return toDetail(p, fav, prev, next, parseExamples(p.getExamplesJson()));
    }

    private GrammarPointListItemResponse toListItem(GrammarPoint p, boolean favourite) {
        GrammarPointListItemResponse r = new GrammarPointListItemResponse();
        r.setId(p.getId());
        r.setGroupId(p.getGroup().getId());
        r.setGroupName(p.getGroup().getName());
        r.setTitle(p.getTitle());
        r.setMeaningSummary(shortenMeaning(p.getMeaning()));
        r.setSortOrder(p.getSortOrder() != null ? p.getSortOrder() : 0);
        r.setFavourite(favourite);
        return r;
    }

    private GrammarPointDetailResponse toDetail(
            GrammarPoint p,
            boolean favourite,
            UUID previousId,
            UUID nextId,
            List<GrammarExampleResponse> examples) {
        GrammarPointDetailResponse r = new GrammarPointDetailResponse();
        r.setId(p.getId());
        r.setGroupId(p.getGroup().getId());
        r.setJlptLevel(p.getGroup().getJlptLevel());
        r.setGroupName(p.getGroup().getName());
        r.setTitle(p.getTitle());
        r.setFormula(p.getFormula());
        r.setMeaning(p.getMeaning());
        r.setContext(p.getContext());
        r.setNote(p.getNote());
        r.setExamples(examples);
        r.setSortOrder(p.getSortOrder() != null ? p.getSortOrder() : 0);
        r.setFavourite(favourite);
        r.setPreviousId(previousId);
        r.setNextId(nextId);
        return r;
    }

    private List<GrammarExampleResponse> parseExamples(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> arr = objectMapper.readValue(json, new TypeReference<>() {});
            List<GrammarExampleResponse> out = new ArrayList<>();
            for (Map<String, Object> m : arr) {
                GrammarExampleResponse e = new GrammarExampleResponse();
                e.setJa(Objects.toString(m.get("ja"), null));
                e.setVi(Objects.toString(m.get("vi"), null));
                Object reg = m.get("register");
                e.setRegister(reg != null ? reg.toString() : null);
                out.add(e);
            }
            return out;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static String shortenMeaning(String meaning) {
        if (meaning == null) {
            return "";
        }
        String t = meaning.replace('\n', ' ').trim();
        return t.length() <= 120 ? t : t.substring(0, 117) + "…";
    }

    private static String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new BadRequestException("Thiếu cấp độ JLPT");
        }
        String u = level.trim().toUpperCase(Locale.ROOT);
        if (!LEVEL_ORDER.contains(u)) {
            throw new BadRequestException("Cấp độ không hợp lệ (N5–N1)");
        }
        return u;
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

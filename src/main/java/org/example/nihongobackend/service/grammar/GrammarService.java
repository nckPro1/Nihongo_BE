package org.example.nihongobackend.service.grammar;

import org.example.nihongobackend.dto.response.grammar.GrammarGroupSummaryResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarLevelSummaryResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarPointDetailResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarPointListItemResponse;

import java.util.List;
import java.util.UUID;

public interface GrammarService {

    List<GrammarLevelSummaryResponse> listLevels();

    List<GrammarGroupSummaryResponse> listGroups(String level);

    List<GrammarPointListItemResponse> listPoints(String level, UUID groupId, String q);

    GrammarPointDetailResponse getPointDetail(UUID pointId);
}

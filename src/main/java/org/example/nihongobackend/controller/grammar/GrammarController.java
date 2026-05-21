package org.example.nihongobackend.controller.grammar;

import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarGroupSummaryResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarLevelSummaryResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarPointDetailResponse;
import org.example.nihongobackend.dto.response.grammar.GrammarPointListItemResponse;
import org.example.nihongobackend.service.grammar.GrammarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grammar")
public class GrammarController {

    private final GrammarService grammarService;

    public GrammarController(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    @GetMapping("/levels")
    public ApiResponse<List<GrammarLevelSummaryResponse>> levels() {
        return ApiResponse.success("OK", grammarService.listLevels());
    }

    @GetMapping("/groups")
    public ApiResponse<List<GrammarGroupSummaryResponse>> groups(@RequestParam("level") String level) {
        return ApiResponse.success("OK", grammarService.listGroups(level));
    }

    @GetMapping("/points")
    public ApiResponse<List<GrammarPointListItemResponse>> points(
            @RequestParam("level") String level,
            @RequestParam(value = "groupId", required = false) UUID groupId,
            @RequestParam(value = "q", required = false) String q) {
        return ApiResponse.success("OK", grammarService.listPoints(level, groupId, q));
    }

    @GetMapping("/points/{id}")
    public ApiResponse<GrammarPointDetailResponse> pointDetail(@PathVariable("id") UUID id) {
        return ApiResponse.success("OK", grammarService.getPointDetail(id));
    }
}

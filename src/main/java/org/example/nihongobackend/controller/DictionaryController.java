package org.example.nihongobackend.controller;

import org.example.nihongobackend.service.DictionaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dictionary")
@CrossOrigin(origins = "*")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/vi-ja")
    public ResponseEntity<?> vietToJa(@RequestParam("word") String word) {
        try {
            if (!StringUtils.hasText(word)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tham số word không được để trống"));
            }
            return ResponseEntity.ok(dictionaryService.searchVietToNhat(word.trim()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Lỗi tra cứu"));
        }
    }

    @GetMapping("/ja-vi")
    public ResponseEntity<?> jaToViet(@RequestParam("word") String word) {
        try {
            if (!StringUtils.hasText(word)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tham số word không được để trống"));
            }
            return ResponseEntity.ok(dictionaryService.searchNhatToViet(word.trim()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Lỗi tra cứu"));
        }
    }
}

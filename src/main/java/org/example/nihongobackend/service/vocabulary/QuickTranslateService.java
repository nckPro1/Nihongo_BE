package org.example.nihongobackend.service.vocabulary;

import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.service.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Map;

/**
 * Tra cứu nhanh hai chiều Vi↔Ja qua DeepSeek API.
 */
@Service
public class QuickTranslateService {

    private static final Logger log = LoggerFactory.getLogger(QuickTranslateService.class);


    private final DictionaryService dictionaryService;

    public QuickTranslateService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public QuickTranslateResponse translate(String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        text = Normalizer.normalize(text, Normalizer.Form.NFC);
        if (text.isEmpty()) {
            throw new BadRequestException("Nội dung tra cứu không được để trống");
        }
        if (text.length() > VocabularyLookupLimits.MAX_TEXT_CHARS) {
            throw new BadRequestException(
                    "Tối đa " + VocabularyLookupLimits.MAX_TEXT_CHARS + " ký tự (chỉ hỗ trợ từ / cụm ngắn)");
        }

        try {
            if (VocabularyInputClassifier.looksLikeVietnamese(text)) {
                Map<String, String> m = dictionaryService.searchVietToNhat(text);
                QuickTranslateResponse r = new QuickTranslateResponse();
                r.setKanji(m.get("kanji"));
                r.setRomaji("");
                r.setMeaning(m.get("keyword"));
                r.setDirection("vi_jp");
                return r;
            }
            Map<String, String> m = dictionaryService.searchNhatToViet(text);
            QuickTranslateResponse r = new QuickTranslateResponse();
            r.setKanji(m.get("keyword"));
            r.setRomaji("");
            r.setMeaning(m.get("meaningFormatted"));
            r.setDirection("jp_vi");
            return r;
        } catch (Exception e) {
            log.warn("[QuickTranslate] direction={} textLen={} err={}",
                    VocabularyInputClassifier.looksLikeVietnamese(text) ? "vi_jp" : "jp_vi",
                    text.length(),
                    e.toString(),
                    e);
            throw new BadRequestException(
                    "Không dịch được: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
            );
        }
    }
}

package org.example.nihongobackend.service.impl.vocabulary;

import org.example.nihongobackend.dto.response.vocabulary.QuickTranslateResponse;
import org.example.nihongobackend.dto.response.vocabulary.VocabularyLookupResponse;
import org.example.nihongobackend.exception.BadRequestException;
import org.example.nihongobackend.service.vocabulary.QuickTranslateService;
import org.example.nihongobackend.service.vocabulary.VocabularyInputClassifier;
import org.example.nihongobackend.service.vocabulary.VocabularyLookupLimits;
import org.example.nihongobackend.service.vocabulary.VocabularyLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
public class QuickTranslateServiceImpl implements QuickTranslateService {

    private static final Logger log = LoggerFactory.getLogger(QuickTranslateServiceImpl.class);

    private final VocabularyLookupService vocabularyLookupService;

    public QuickTranslateServiceImpl(VocabularyLookupService vocabularyLookupService) {
        this.vocabularyLookupService = vocabularyLookupService;
    }

    @Override
    public QuickTranslateResponse translate(String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        text = Normalizer.normalize(text, Normalizer.Form.NFC);
        if (text.isEmpty()) {
            throw new BadRequestException("Nội dung tra cứu không được để trống");
        }
        if (text.length() > VocabularyLookupLimits.MAX_TEXT_CHARS) {
            throw new BadRequestException(
                    "Từ điển chỉ tra từ/cụm ngắn (tối đa " + VocabularyLookupLimits.MAX_TEXT_CHARS +
                    " ký tự). Để dịch câu dài, dùng 'Dịch đoạn văn' bên dưới.");
        }

        try {
            // Use new 4-layer vocabulary lookup service
            VocabularyLookupResponse lookupResult = vocabularyLookupService.lookup(text, null);

            QuickTranslateResponse response = new QuickTranslateResponse();
            if (lookupResult.isSuccess()) {
                response.setKanji(lookupResult.getWord());
                response.setRomaji(lookupResult.getReading() != null ? lookupResult.getReading() : "");
                response.setMeaning(lookupResult.getMeaningVi());
                response.setDirection(VocabularyInputClassifier.looksLikeVietnamese(text) ? "vi_jp" : "jp_vi");
            } else {
                throw new BadRequestException("Không tìm thấy từ trong từ điển");
            }

            return response;
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

package org.example.nihongobackend.service.vocabulary;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Gợi ý hướng tra cứu: tiếng Việt có dấu / ký tự đặc trưng → ưu tiên luồng Việt→Nhật.
 */
public final class VocabularyInputClassifier {

    private static final Pattern VIETNAMESE_MARKED =
            Pattern.compile("[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵĐđ]");

    /**
     * Latin-1 Supplement + Latin Extended-A/B + Latin Extended Additional (ô, ơ, ệ…),
     * bổ sung ký tự 00C0–00FF mà regex tay dễ sót.
     */
    private static final Pattern VIET_LATIN_EXTENDED_BLOCKS = Pattern.compile(
            "[\\u00C0-\\u024F\\u1E00-\\u1EFF]");

    /** Hiragana, Katakana, CJK thường gặp (kanji). */
    private static final Pattern JAPANESE_SCRIPT = Pattern.compile(
            "[\\p{IsHiragana}\\p{IsKatakana}\\u4E00-\\u9FFF\\u3000-\\u303F]");

    private VocabularyInputClassifier() {
    }

    /**
     * Có dấu / chữ đặc trưng tiếng Việt. Luôn chuẩn hóa NFC trước khi kiểm tra
     * (tránh NFD: chữ + dấu tách khiến regex ký tự đơn không khớp).
     */
    public static boolean looksLikeVietnamese(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        String nfc = Normalizer.normalize(normalized.trim(), Normalizer.Form.NFC);
        if (VIETNAMESE_MARKED.matcher(nfc).find()) {
            return true;
        }
        return VIET_LATIN_EXTENDED_BLOCKS.matcher(nfc).find();
    }

    /** Có ký tự Nhật (kana/kanji) — dùng gợi ý nhánh Nhật→Việt khi không phải tiếng Việt có dấu. */
    public static boolean looksLikeJapaneseScript(String normalized) {
        return normalized != null && JAPANESE_SCRIPT.matcher(normalized).find();
    }
}

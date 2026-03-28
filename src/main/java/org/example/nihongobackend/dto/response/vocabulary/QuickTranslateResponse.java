package org.example.nihongobackend.dto.response.vocabulary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tra từ nhanh — chỉ JSON gọn để giảm output token (kanji / romaji / meaning).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickTranslateResponse {

    /**
     * Vi→Ja: dòng chính {@code 仕事 (しごと)}. Ja→Vi: từ/cụm nhập (Nhật).
     */
    private String kanji;
    /** Vi→Ja: để trống (đọc đã gộp trong {@code kanji}). Ja→Vi: để trống. */
    private String romaji;
    /**
     * Vi→Ja: từ gốc tiếng Việt. Ja→Vi: nghĩa Việt + hiragana ({@code nghĩa — よみ}).
     */
    private String meaning;
    /**
     * {@code jp_vi}: tra từ/cụm tiếng Nhật → nghĩa Việt.
     * {@code vi_jp}: đầu vào tiếng Việt → cách nói tiếng Nhật (kanji/kana kèm đọc trong {@code kanji}).
     */
    private String direction;

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public String getRomaji() {
        return romaji;
    }

    public void setRomaji(String romaji) {
        this.romaji = romaji;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}

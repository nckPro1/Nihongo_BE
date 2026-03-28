package org.example.nihongobackend.dto.response.vocabulary;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassageTranslateResponse {

    /** {@code vi_jp} hoặc {@code jp_vi} */
    private String direction;
    /** Câu tiếng Nhật (gốc hoặc bản dịch). */
    private String japanese;
    /** Câu tiếng Việt (gốc hoặc bản dịch). */
    private String vietnamese;
    /** Phiên âm hiragana cho toàn bộ {@link #japanese}. */
    private String hiraganaLine;
    private List<PassageCompoundWord> compounds = new ArrayList<>();

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getJapanese() {
        return japanese;
    }

    public void setJapanese(String japanese) {
        this.japanese = japanese;
    }

    public String getVietnamese() {
        return vietnamese;
    }

    public void setVietnamese(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    public String getHiraganaLine() {
        return hiraganaLine;
    }

    public void setHiraganaLine(String hiraganaLine) {
        this.hiraganaLine = hiraganaLine;
    }

    public List<PassageCompoundWord> getCompounds() {
        return compounds;
    }

    public void setCompounds(List<PassageCompoundWord> compounds) {
        this.compounds = compounds != null ? compounds : new ArrayList<>();
    }
}

package org.example.nihongobackend.dto.response.vocabulary;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Cụm kanji trong câu Nhật — dùng cho hover (đọc + nghĩa).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassageCompoundWord {

    private String surface;
    private String reading;
    private String glossVi;

    public PassageCompoundWord() {
    }

    public PassageCompoundWord(String surface, String reading, String glossVi) {
        this.surface = surface;
        this.reading = reading;
        this.glossVi = glossVi;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getGlossVi() {
        return glossVi;
    }

    public void setGlossVi(String glossVi) {
        this.glossVi = glossVi;
    }
}

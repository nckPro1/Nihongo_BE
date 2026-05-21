package org.example.nihongobackend.dto.request.flashcard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateFlashcardRequest {

    @NotNull(message = "Thiếu Zenigo (projectId)")
    private UUID projectId;

    @NotBlank(message = "Kanji / câu Nhật không được để trống")
    @Size(max = 2000)
    private String kanji;

    @Size(max = 1000)
    private String reading;

    @NotBlank(message = "Nghĩa không được để trống")
    @Size(max = 4000)
    private String meaning;

    @Size(max = 16)
    private String direction;

    @Size(max = 500)
    private String sourceQuery;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
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

    public String getSourceQuery() {
        return sourceQuery;
    }

    public void setSourceQuery(String sourceQuery) {
        this.sourceQuery = sourceQuery;
    }
}

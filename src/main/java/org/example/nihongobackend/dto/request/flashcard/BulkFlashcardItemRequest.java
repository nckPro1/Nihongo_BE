package org.example.nihongobackend.dto.request.flashcard;

import jakarta.validation.constraints.Size;

/** Từng dòng batch — chuỗi rỗng sau trim sẽ bị bỏ qua (skippedInvalid), không làm fail cả lô. */
public class BulkFlashcardItemRequest {

    @Size(max = 2000)
    private String kanji;

    @Size(max = 1000)
    private String reading;

    @Size(max = 4000)
    private String meaning;

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
}

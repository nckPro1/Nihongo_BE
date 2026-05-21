package org.example.nihongobackend.dto.response.flashcard;

public class BulkCreateFlashcardsResponse {

    private int created;
    private int skippedDuplicates;
    private int skippedInvalid;

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getSkippedDuplicates() {
        return skippedDuplicates;
    }

    public void setSkippedDuplicates(int skippedDuplicates) {
        this.skippedDuplicates = skippedDuplicates;
    }

    public int getSkippedInvalid() {
        return skippedInvalid;
    }

    public void setSkippedInvalid(int skippedInvalid) {
        this.skippedInvalid = skippedInvalid;
    }
}

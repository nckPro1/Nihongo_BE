package org.example.nihongobackend.dto.response.grammar;

public class GrammarLevelSummaryResponse {

    private String level;
    private long grammarCount;
    private long groupCount;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getGrammarCount() {
        return grammarCount;
    }

    public void setGrammarCount(long grammarCount) {
        this.grammarCount = grammarCount;
    }

    public long getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(long groupCount) {
        this.groupCount = groupCount;
    }
}

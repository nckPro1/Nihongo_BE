package org.example.nihongobackend.dto.response.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GrammarPointDetailResponse {

    private UUID id;
    private UUID groupId;
    private String jlptLevel;
    private String groupName;
    private String title;
    private String formula;
    private String meaning;
    private String context;
    private String note;
    private List<GrammarExampleResponse> examples = new ArrayList<>();
    private int sortOrder;
    private boolean favourite;
    private UUID previousId;
    private UUID nextId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getJlptLevel() {
        return jlptLevel;
    }

    public void setJlptLevel(String jlptLevel) {
        this.jlptLevel = jlptLevel;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<GrammarExampleResponse> getExamples() {
        return examples;
    }

    public void setExamples(List<GrammarExampleResponse> examples) {
        this.examples = examples;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public UUID getPreviousId() {
        return previousId;
    }

    public void setPreviousId(UUID previousId) {
        this.previousId = previousId;
    }

    public UUID getNextId() {
        return nextId;
    }

    public void setNextId(UUID nextId) {
        this.nextId = nextId;
    }
}

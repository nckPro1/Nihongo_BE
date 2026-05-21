package org.example.nihongobackend.dto.response.grammar;

import java.util.UUID;

public class GrammarPointListItemResponse {

    private UUID id;
    private UUID groupId;
    private String groupName;
    private String title;
    private String meaningSummary;
    private int sortOrder;
    private boolean favourite;

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

    public String getMeaningSummary() {
        return meaningSummary;
    }

    public void setMeaningSummary(String meaningSummary) {
        this.meaningSummary = meaningSummary;
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
}

package org.example.nihongobackend.dto.response.favourite;

import java.util.UUID;

/**
 * Một dòng trong danh sách yêu thích (ngữ pháp, blog, …).
 */
public class FavouriteItemResponse {

    private String targetType;
    private UUID targetId;
    private String createdAt;
    /** Tiêu đề hiển thị (mẫu ngữ pháp hoặc nhãn blog). */
    private String title;
    /** Dòng phụ (nghĩa rút gọn, v.v.). */
    private String detail;
    /** Cấp JLPT — dùng để ghép URL /grammar/{level}/{id}; null nếu không phải grammar. */
    private String grammarJlptLevel;
    /** true nếu mục grammar đã bị xóa khỏi DB nhưng bản ghi favourite còn. */
    private boolean resourceMissing;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getGrammarJlptLevel() {
        return grammarJlptLevel;
    }

    public void setGrammarJlptLevel(String grammarJlptLevel) {
        this.grammarJlptLevel = grammarJlptLevel;
    }

    public boolean isResourceMissing() {
        return resourceMissing;
    }

    public void setResourceMissing(boolean resourceMissing) {
        this.resourceMissing = resourceMissing;
    }
}

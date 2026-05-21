package org.example.nihongobackend.service.vocabulary;

/**
 * Vocabulary lookup limits (từ điển & ngữ cảnh)
 *
 * Giới hạn độ dài cho tra cứu từ vựng - chỉ dùng cho TỪ/CỤM NGẮN, KHÔNG phải câu dài.
 * Mục đích:
 * - Ngăn user nhập toàn bộ đoạn văn/bài học
 * - Tối ưu cost AI API (Layer 4)
 * - Đảm bảo response quality (từ điển tra từ, không phải dịch câu)
 *
 * Ví dụ hợp lệ:
 * - "chó", "con chó" (7 chars)
 * - "điện thoại thông minh" (21 chars)
 * - "máy tính xách tay" (17 chars)
 * - "食べる", "食べ物" (3-4 chars)
 */
public final class VocabularyLookupLimits {

    /**
     * Max characters for vocabulary lookup
     * 50 chars = đủ cho compound words (2-4 từ), không đủ cho câu hoàn chỉnh
     */
    public static final int MAX_TEXT_CHARS = 50;

    /**
     * Recommended max for single words/short phrases
     */
    public static final int RECOMMENDED_MAX_CHARS = 30;

    private VocabularyLookupLimits() {
    }
}

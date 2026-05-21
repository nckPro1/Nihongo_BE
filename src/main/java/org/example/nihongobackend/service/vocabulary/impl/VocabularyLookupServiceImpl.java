package org.example.nihongobackend.service.vocabulary.impl;

import org.example.nihongobackend.dto.response.vocabulary.VocabularyLookupResponse;
import org.example.nihongobackend.entity.AiVocabCache;
import org.example.nihongobackend.entity.CommunityVocab;
import org.example.nihongobackend.entity.OwnVocab;
import org.example.nihongobackend.repository.AiVocabCacheRepository;
import org.example.nihongobackend.repository.CommunityVocabRepository;
import org.example.nihongobackend.repository.OwnVocabRepository;
import org.example.nihongobackend.service.vocabulary.QuotaExceededException;
import org.example.nihongobackend.service.vocabulary.VocabularyAiService;
import org.example.nihongobackend.service.vocabulary.VocabularyLookupService;
import org.example.nihongobackend.service.vocabulary.VocabularyStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of 4-layer vocabulary lookup strategy
 * ✅ Layer 1: Own Curated Vocab (7.9k JLPT words) - <10ms, $0
 * ✅ Layer 2: Community Dict (281k ODVP words) - <20ms, $0
 * ✅ Layer 3: Shared AI Cache (global, grows over time) - <30ms, $0
 * ✅ Layer 4: AI Service (DeepSeek) with quota - ~800ms, $0.0001
 *
 * Expected coverage: 60% (L1) + 25% (L2) + 14% (L3) + 1% (L4) = 100%
 * Expected cost reduction: 99% (from $100/month to $1/month per 10k users)
 */
@Service
public class VocabularyLookupServiceImpl implements VocabularyLookupService {

    private static final Logger log = LoggerFactory.getLogger(VocabularyLookupServiceImpl.class);

    private final OwnVocabRepository ownVocabRepository;
    private final CommunityVocabRepository communityVocabRepository;
    private final AiVocabCacheRepository aiVocabCacheRepository;
    private final VocabularyAiService vocabularyAiService;

    public VocabularyLookupServiceImpl(
            OwnVocabRepository ownVocabRepository,
            CommunityVocabRepository communityVocabRepository,
            AiVocabCacheRepository aiVocabCacheRepository,
            VocabularyAiService vocabularyAiService) {
        this.ownVocabRepository = ownVocabRepository;
        this.communityVocabRepository = communityVocabRepository;
        this.aiVocabCacheRepository = aiVocabCacheRepository;
        this.vocabularyAiService = vocabularyAiService;
    }

    @Override
    @Transactional
    public VocabularyLookupResponse lookup(String query, String userId) {
        long startTime = System.currentTimeMillis();

        if (query == null || query.isBlank()) {
            return VocabularyLookupResponse.builder()
                    .success(false)
                    .query(query)
                    .build();
        }

        String cleanQuery = query.strip();
        log.debug("[Vocab Lookup] Query: {}, User: {}", cleanQuery, userId);

        // Layer 1: Own curated JLPT vocab
        Optional<OwnVocab> ownVocabResult = ownVocabRepository.findBestMatch(cleanQuery);
        if (ownVocabResult.isPresent()) {
            OwnVocab vocab = ownVocabResult.get();
            long latency = System.currentTimeMillis() - startTime;

            log.info("[Vocab] L1 HIT: {} → {} ({} ms)", cleanQuery, vocab.getMeaningVi(), latency);

            // Increment search count asynchronously
            try {
                vocab.incrementSearchCount();
                ownVocabRepository.save(vocab);
            } catch (Exception e) {
                log.warn("[Vocab] Failed to update search count: {}", e.getMessage());
            }

            return VocabularyLookupResponse.builder()
                    .success(true)
                    .query(cleanQuery)
                    .word(vocab.getWord())
                    .reading(vocab.getReading())
                    .meaningEn(vocab.getMeaningEn())
                    .meaningVi(vocab.getMeaningVi())
                    .level(vocab.getLevel())
                    .partOfSpeech(vocab.getPartOfSpeech())
                    .source("own_vocab")
                    .quality(vocab.getIsVerified() ? "verified" : "unverified")
                    .latencyMs(latency)
                    .build();
        }

        // Layer 2: Community Vietnamese dictionary (ODVP)
        Optional<CommunityVocab> communityResult = communityVocabRepository.findBestMatch(cleanQuery);
        if (communityResult.isPresent()) {
            CommunityVocab vocab = communityResult.get();
            long latency = System.currentTimeMillis() - startTime;

            log.info("[Vocab] L2 HIT: {} → {} ({} ms)", cleanQuery, vocab.getMeaningVi(), latency);

            // Increment search count
            try {
                vocab.incrementSearchCount();
                communityVocabRepository.save(vocab);
            } catch (Exception e) {
                log.warn("[Vocab] Failed to update search count (L2): {}", e.getMessage());
            }

            return VocabularyLookupResponse.builder()
                    .success(true)
                    .query(cleanQuery)
                    .word(vocab.getWord())
                    .reading(vocab.getReading())
                    .meaningEn(null)  // Community vocab doesn't have English
                    .meaningVi(vocab.getMeaningVi())
                    .level(null)  // No JLPT level in community vocab
                    .partOfSpeech(vocab.getPartOfSpeech())
                    .source("community_vocab")
                    .quality("community")  // Community-contributed, not verified
                    .latencyMs(latency)
                    .build();
        }

        // Layer 3: Shared AI cache (global cache from previous AI lookups)
        Optional<AiVocabCache> aiCacheResult = aiVocabCacheRepository.findBestMatch(cleanQuery);
        if (aiCacheResult.isPresent()) {
            AiVocabCache vocab = aiCacheResult.get();
            long latency = System.currentTimeMillis() - startTime;

            log.info("[Vocab] L3 HIT: {} → {} ({} ms, cached from {})",
                    cleanQuery, vocab.getMeaningVi(), latency, vocab.getModel());

            // Increment query count (cache hit tracking)
            try {
                vocab.incrementQueryCount();
                aiVocabCacheRepository.save(vocab);
            } catch (Exception e) {
                log.warn("[Vocab] Failed to update cache hit count (L3): {}", e.getMessage());
            }

            return VocabularyLookupResponse.builder()
                    .success(true)
                    .query(cleanQuery)
                    .word(vocab.getWord())
                    .reading(vocab.getReading())
                    .meaningEn(null)  // AI cache doesn't have English
                    .meaningVi(vocab.getMeaningVi())
                    .level(null)  // No JLPT level
                    .partOfSpeech(null)
                    .source("ai_cache")
                    .quality(vocab.getIsVerified() ? "verified" : "ai_generated")
                    .latencyMs(latency)
                    .build();
        }

        // Layer 4: AI Service (DeepSeek/Gemini) with quota check
        try {
            log.info("[Vocab] L1/L2/L3 MISS → Calling L4 AI: {}", cleanQuery);
            VocabularyLookupResponse aiResult = vocabularyAiService.translateWithAi(cleanQuery, userId);

            log.info("[Vocab] L4 AI SUCCESS: {} → {} ({} ms)",
                    cleanQuery, aiResult.getMeaningVi(), aiResult.getLatencyMs());

            return aiResult;

        } catch (QuotaExceededException e) {
            // User exceeded free quota
            long latency = System.currentTimeMillis() - startTime;
            log.warn("[Vocab] L4 QUOTA EXCEEDED for user {}: {}", userId, cleanQuery);

            return VocabularyLookupResponse.builder()
                    .success(false)
                    .query(cleanQuery)
                    .latencyMs(latency)
                    .build();

        } catch (Exception e) {
            // AI service failed
            long latency = System.currentTimeMillis() - startTime;
            log.error("[Vocab] L4 AI FAILED: {} - {}", cleanQuery, e.getMessage());

            return VocabularyLookupResponse.builder()
                    .success(false)
                    .query(cleanQuery)
                    .latencyMs(latency)
                    .build();
        }
    }

    @Override
    public VocabularyStatsResponse getStats() {
        long totalWords = ownVocabRepository.count();
        long verifiedWords = ownVocabRepository.countByIsVerified(true);

        // Count by JLPT level
        Map<String, Long> byLevel = new HashMap<>();
        byLevel.put("N5", ownVocabRepository.countByLevel("N5"));
        byLevel.put("N4", ownVocabRepository.countByLevel("N4"));
        byLevel.put("N3", ownVocabRepository.countByLevel("N3"));
        byLevel.put("N2", ownVocabRepository.countByLevel("N2"));
        byLevel.put("N1", ownVocabRepository.countByLevel("N1"));

        // Count by source
        Map<String, Long> bySource = new HashMap<>();
        bySource.put("own_vocab", totalWords);
        bySource.put("community_vocab", communityVocabRepository.count());
        bySource.put("ai_cache", aiVocabCacheRepository.count());

        // Calculate total searches
        long totalSearches = ownVocabRepository.findAll().stream()
                .mapToLong(OwnVocab::getSearchCount)
                .sum();

        return new VocabularyStatsResponse(totalWords, byLevel, verifiedWords, totalSearches, bySource);
    }

    @Override
    public Map<String, Object> debugSearch(String word) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("query", word);

        // Check Layer 1
        Optional<OwnVocab> l1Result = ownVocabRepository.findBestMatch(word);
        if (l1Result.isPresent()) {
            OwnVocab vocab = l1Result.get();
            Map<String, Object> l1Info = new HashMap<>();
            l1Info.put("found", true);
            l1Info.put("word", vocab.getWord());
            l1Info.put("reading", vocab.getReading());
            l1Info.put("meaningVi", vocab.getMeaningVi());
            l1Info.put("meaningEn", vocab.getMeaningEn());
            l1Info.put("level", vocab.getLevel());
            debugInfo.put("layer1_ownVocab", l1Info);
        } else {
            Map<String, Object> l1Info = new HashMap<>();
            l1Info.put("found", false);
            debugInfo.put("layer1_ownVocab", l1Info);
        }

        // Check Layer 2
        Optional<CommunityVocab> l2Result = communityVocabRepository.findBestMatch(word);
        if (l2Result.isPresent()) {
            CommunityVocab vocab = l2Result.get();
            Map<String, Object> l2Info = new HashMap<>();
            l2Info.put("found", true);
            l2Info.put("word", vocab.getWord());
            l2Info.put("reading", vocab.getReading());
            l2Info.put("meaningVi", vocab.getMeaningVi());
            debugInfo.put("layer2_communityVocab", l2Info);
        } else {
            Map<String, Object> l2Info = new HashMap<>();
            l2Info.put("found", false);
            debugInfo.put("layer2_communityVocab", l2Info);
        }

        // Check Layer 3
        Optional<AiVocabCache> l3Result = aiVocabCacheRepository.findBestMatch(word);
        if (l3Result.isPresent()) {
            AiVocabCache vocab = l3Result.get();
            Map<String, Object> l3Info = new HashMap<>();
            l3Info.put("found", true);
            l3Info.put("word", vocab.getWord());
            l3Info.put("reading", vocab.getReading());
            l3Info.put("meaningVi", vocab.getMeaningVi());
            l3Info.put("model", vocab.getModel());
            debugInfo.put("layer3_aiCache", l3Info);
        } else {
            Map<String, Object> l3Info = new HashMap<>();
            l3Info.put("found", false);
            debugInfo.put("layer3_aiCache", l3Info);
        }

        // Database stats
        debugInfo.put("totalOwnVocab", ownVocabRepository.count());
        debugInfo.put("totalCommunityVocab", communityVocabRepository.count());
        debugInfo.put("totalAiCache", aiVocabCacheRepository.count());

        return debugInfo;
    }

    @Override
    public void clearCacheEntry(String word) {
        // Clear from all cache layers
        try {
            // Delete from AiVocabCache (Layer 3)
            aiVocabCacheRepository.findBestMatch(word).ifPresent(cache -> {
                aiVocabCacheRepository.delete(cache);
                log.info("[Vocab] Cleared cache entry: {}", word);
            });
        } catch (Exception e) {
            log.warn("[Vocab] Failed to clear cache for {}: {}", word, e.getMessage());
        }
    }
}

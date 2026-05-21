package org.example.nihongobackend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Cache kết quả tra từ từ DeepSeek.
     * - TTL 24h: từ vựng không thay đổi theo giờ
     * - Max 5000 entry: đủ cho production nhỏ, không chiếm quá nhiều RAM (~5MB)
     */
    public static final String DICT_JP_VI = "dict_jp_vi";
    public static final String DICT_VI_JP = "dict_vi_jp";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(DICT_JP_VI, DICT_VI_JP);
        manager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
        );
        return manager;
    }
}

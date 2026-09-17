package com.campus.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置，用于缓存变化频率低的数据（分类列表、新闻列表等）
 */
@Configuration
public class CacheConfig {

    /**
     * 分类列表缓存：10 分钟过期，最大 100 条
     */
    @Bean
    public Cache<String, Object> categoryCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    /**
     * 新闻列表缓存：5 分钟过期，最大 100 条
     */
    @Bean
    public Cache<String, Object> newsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    /**
     * 仪表盘统计缓存：1 分钟过期，单条
     */
    @Bean
    public Cache<String, Object> dashboardCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }
}

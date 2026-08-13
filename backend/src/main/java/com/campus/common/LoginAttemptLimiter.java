package com.campus.common;

import com.campus.exception.GlobalException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败限流：基于 Caffeine 本地缓存，按 key 独立过期淘汰，
 * 避免原实现超出容量后全量清空导致限流失效的问题。
 */
@Component
public class LoginAttemptLimiter {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_SECONDS = 15 * 60L;
    private static final int MAX_CACHED_KEYS = 10000;

    private final Cache<String, AttemptInfo> attempts = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW_SECONDS, TimeUnit.SECONDS)
            .maximumSize(MAX_CACHED_KEYS)
            .build();

    private static class AttemptInfo {
        int failCount;
    }

    public void checkAllowed(String key) {
        if (key == null) {
            return;
        }
        AttemptInfo info = attempts.getIfPresent(key);
        if (info != null && info.failCount >= MAX_FAILURES) {
            throw new GlobalException("密码错误次数过多，请15分钟后再试");
        }
    }

    public void recordFailure(String key) {
        if (key == null) {
            return;
        }
        attempts.asMap().compute(key, (k, info) -> {
            AttemptInfo current = (info == null) ? new AttemptInfo() : info;
            current.failCount++;
            return current;
        });
    }

    public void recordSuccess(String key) {
        if (key == null) {
            return;
        }
        attempts.invalidate(key);
    }
}
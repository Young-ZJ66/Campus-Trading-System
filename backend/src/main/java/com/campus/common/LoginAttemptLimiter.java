package com.campus.common;

import com.campus.exception.GlobalException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptLimiter {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final int MAX_CACHED_KEYS = 10000;

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    private static class AttemptInfo {
        int failCount;
        long firstFailAt;
    }

    public void checkAllowed(String key) {
        if (key == null) {
            return;
        }
        AttemptInfo info = attempts.get(key);
        if (info != null) {
            if (info.failCount >= MAX_FAILURES && System.currentTimeMillis() - info.firstFailAt < WINDOW_MILLIS) {
                throw new GlobalException("密码错误次数过多，请15分钟后再试");
            }
            if (System.currentTimeMillis() - info.firstFailAt >= WINDOW_MILLIS) {
                attempts.remove(key);
            }
        }
        if (attempts.size() > MAX_CACHED_KEYS) {
            attempts.clear();
        }
    }

    public void recordFailure(String key) {
        if (key == null) {
            return;
        }
        attempts.compute(key, (k, info) -> {
            if (info == null || System.currentTimeMillis() - info.firstFailAt >= WINDOW_MILLIS) {
                AttemptInfo fresh = new AttemptInfo();
                fresh.failCount = 1;
                fresh.firstFailAt = System.currentTimeMillis();
                return fresh;
            }
            info.failCount++;
            return info;
        });
    }

    public void recordSuccess(String key) {
        if (key == null) {
            return;
        }
        attempts.remove(key);
    }
}
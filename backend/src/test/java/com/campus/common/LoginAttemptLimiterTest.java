package com.campus.common;

import com.campus.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 登录限流器单元测试：验证失败次数达到上限后锁定、成功后重置
 */
class LoginAttemptLimiterTest {

    private LoginAttemptLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginAttemptLimiter();
    }

    @Test
    void shouldLockAfterMaxFailures() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("20220001");
        }
        assertThrows(GlobalException.class, () -> limiter.checkAllowed("20220001"));
    }

    @Test
    void shouldAllowBelowMaxFailures() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("20220001");
        }
        assertDoesNotThrow(() -> limiter.checkAllowed("20220001"));
    }

    @Test
    void shouldResetAfterSuccess() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("20220001");
        }
        limiter.recordSuccess("20220001");
        assertDoesNotThrow(() -> limiter.checkAllowed("20220001"));
    }

    @Test
    void shouldIgnoreNullKey() {
        assertDoesNotThrow(() -> {
            limiter.checkAllowed(null);
            limiter.recordFailure(null);
            limiter.recordSuccess(null);
        });
    }
}
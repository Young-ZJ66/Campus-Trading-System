package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.PointMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.PointRecord;
import com.campus.pojo.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 积分服务签到防重单元测试：验证并发重复签到由唯一索引兜底并抛出业务异常
 */
@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private PointMapper pointMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    void shouldRejectSignInWhenAlreadySigned() {
        when(pointMapper.checkTodaySignIn(1L)).thenReturn(1);
        assertThrows(GlobalException.class, () -> pointService.signIn(1L));
    }

    @Test
    void shouldRejectConcurrentDuplicateSignIn() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setPoints(10);

        when(pointMapper.checkTodaySignIn(1L)).thenReturn(0);
        when(sysUserMapper.selectByIdForUpdate(1L)).thenReturn(user);
        // 模拟并发下唯一索引冲突
        when(pointMapper.insertRecord(any(PointRecord.class))).thenThrow(new DuplicateKeyException("duplicate"));

        assertThrows(GlobalException.class, () -> pointService.signIn(1L));
    }
}
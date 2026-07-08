package com.campus.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointRecord {
    private Long recordId;
    private Long userId;
    private Integer changeType; // 0-签到获取, 1-交易获取, 2-兑换消耗
    private Integer changeAmount;
    private Integer balanceAfter;
    private LocalDateTime createTime;
}

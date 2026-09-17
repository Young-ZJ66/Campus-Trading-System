package com.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 实时通知消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    /** 通知类型: ORDER_CREATED, ORDER_COMPLETED, ORDER_CANCELLED, COMMENT_ADDED */
    private String type;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 相关资源ID（商品ID/订单ID） */
    private Long relatedId;
    /** 发送时间 */
    private LocalDateTime time;

    public Notification(String type, String title, String content, Long relatedId) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedId = relatedId;
        this.time = LocalDateTime.now();
    }
}

package com.campus.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsComment {
    private Long commentId;
    private Long goodsId;
    private Long userId;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;
    
    // 扩展字段
    private String nickname;
    private String avatar;
    private String parentNickname;
}
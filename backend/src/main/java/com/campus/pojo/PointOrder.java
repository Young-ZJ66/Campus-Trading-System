package com.campus.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointOrder {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long itemId;
    private Integer pointsUsed;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段
    private String itemName;
    private String itemImage;
    private String userNickname;
    private String userStudentNo;
}

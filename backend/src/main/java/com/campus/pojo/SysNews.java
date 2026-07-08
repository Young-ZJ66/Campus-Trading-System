package com.campus.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysNews {
    private Long newsId;
    private String title;
    private String coverImage;
    private String content;
    private Integer viewCount;
    private Integer isTop;
    private Integer status;
    private LocalDateTime createTime;
}
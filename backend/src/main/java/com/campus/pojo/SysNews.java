package com.campus.pojo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysNews {
    private Long newsId;
    @NotBlank(message = "资讯标题不能为空")
    @Size(max = 200, message = "资讯标题最长200个字符")
    private String title;
    @Size(max = 500, message = "封面图路径过长")
    private String coverImage;
    @NotBlank(message = "资讯内容不能为空")
    @Size(max = 50000, message = "资讯内容过长")
    private String content;
    private Integer viewCount;
    private Integer isTop;
    private Integer status;
    private LocalDateTime createTime;
}
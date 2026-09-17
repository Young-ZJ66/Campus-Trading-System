package com.campus.pojo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsComment {
    private Long commentId;
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    private Long userId;
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容最长1000个字符")
    private String content;
    private Long parentId;
    private LocalDateTime createTime;

    private String nickname;
    private String avatar;
    private String parentNickname;
}
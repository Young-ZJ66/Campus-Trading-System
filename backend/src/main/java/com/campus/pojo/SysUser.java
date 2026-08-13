package com.campus.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysUser {
    private Long userId;
    private String studentNo;
    private String nickname;
    private String password;
    private String phone;
    private String avatar;
    private Integer points;
    private Integer status;
    private Integer role; // 0-普通用户, 1-管理员
    private LocalDateTime createTime;
}
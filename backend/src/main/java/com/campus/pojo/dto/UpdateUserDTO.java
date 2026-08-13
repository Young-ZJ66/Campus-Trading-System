package com.campus.pojo.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String nickname;
    private String phone;
    private String avatar;
}
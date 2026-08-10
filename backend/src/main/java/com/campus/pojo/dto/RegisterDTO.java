package com.campus.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z]{6,20}$", message = "学号格式不正确")
    private String studentNo;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 30, message = "昵称长度需在1-30之间")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32位之间")
    private String password;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

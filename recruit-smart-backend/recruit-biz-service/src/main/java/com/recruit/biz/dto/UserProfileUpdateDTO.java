package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "个人信息修改参数")
public class UserProfileUpdateDTO {

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 64, message = "真实姓名不能超过64个字符")
    private String realName;

    @Schema(description = "手机号", example = "13900000001")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
}

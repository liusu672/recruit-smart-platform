package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员修改用户资料参数")
public class AdminUserUpdateDTO {

    @Size(max = 64, message = "真实姓名不能超过64个字符")
    @Schema(description = "真实姓名")
    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号，传空字符串表示清空")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    @Schema(description = "邮箱，传空字符串表示清空")
    private String email;
}

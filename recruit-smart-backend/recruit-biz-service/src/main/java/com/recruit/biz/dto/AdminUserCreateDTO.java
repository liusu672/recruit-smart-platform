package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员创建用户参数")
public class AdminUserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度为4到32位")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "登录用户名", example = "hr02")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6到32位")
    @Schema(description = "初始密码")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 64, message = "真实姓名不能超过64个字符")
    @Schema(description = "真实姓名", example = "李明")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13900000002")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    @Schema(description = "邮箱", example = "liming@example.com")
    private String email;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色ID")
    private Long roleId;
}

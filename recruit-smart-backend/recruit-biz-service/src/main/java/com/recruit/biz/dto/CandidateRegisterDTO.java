package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "候选人注册参数")
public class CandidateRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度为4到32位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6到32位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String gender;

    @Min(value = 16, message = "年龄不能小于16岁")
    @Max(value = 80, message = "年龄不能大于80岁")
    private Integer age;

    private String education;
    private String school;
    private String major;

    @Min(value = 0, message = "工作年限不能小于0")
    private Integer yearsOfExperience;
}

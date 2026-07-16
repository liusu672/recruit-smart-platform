package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "候选人创建参数")
public class CandidateCreateDTO {
    @Schema(description = "姓名",example = "吴家乐")
    @NotBlank(message = "姓名不能为空")
    private String name;
    @Schema(description = "手机号",example = "15289566888")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式不正确")
    private String phone;
    @Schema(description = "邮箱",example = "25542158687@gmail.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    @Schema(description = "年龄",example = "21")
    @Min(value = 16, message = "年龄不能小于16岁")
    @Max(value = 80, message = "年龄不能大于80岁")
    private Integer age;
    @Schema(description = "学历",example = "本科")
    private String education;
    @Schema(description = "性别",example = "男")
    private String gender;
    @Schema(description = "学校",example = "武汉理工大学")
    private String school;
    @Schema(description = "专业",example = "软件工程")
    private String major;
    @Schema(description = "工作年限",example = "0")
    @Min(value = 0, message = "工作年限不能小于0")
    //@Max(value = 60, message = "工作年限不能大于60")
    private Integer yearsOfExperience;
    @Schema(description = "候选人来源",example = "HR_IMPORT")
    private String source;
}

package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "候选人本人求职资料修改参数")
public class CandidateSelfUpdateDTO {

    @Schema(description = "性别", example = "男")
    @Pattern(regexp = "^(男|女|其他)$", message = "性别只能是男、女或其他")
    private String gender;

    @Schema(description = "年龄", example = "24")
    @Min(value = 16, message = "年龄不能小于16岁")
    @Max(value = 80, message = "年龄不能大于80岁")
    private Integer age;

    @Schema(description = "最高学历", example = "本科")
    @Size(max = 64, message = "学历不能超过64个字符")
    private String education;

    @Schema(description = "毕业学校", example = "武汉理工大学")
    @Size(max = 128, message = "学校不能超过128个字符")
    private String school;

    @Schema(description = "专业", example = "软件工程")
    @Size(max = 128, message = "专业不能超过128个字符")
    private String major;

    @Schema(description = "工作年限", example = "2")
    @Min(value = 0, message = "工作年限不能小于0")
    private Integer yearsOfExperience;
}

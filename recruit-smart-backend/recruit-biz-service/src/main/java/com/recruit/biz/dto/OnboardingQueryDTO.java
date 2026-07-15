package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "HR入职流程列表查询参数")
public class OnboardingQueryDTO {

    @Schema(description = "入职流程状态", example = "REVIEWING")
    @Pattern(
            regexp = "^(PENDING|REVIEWING|APPROVED|ONBOARDED|CANCELED)$",
            message = "入职流程状态不正确"
    )
    private String status;

    @Schema(description = "材料状态", example = "REVIEWING")
    @Pattern(
            regexp = "^(PENDING|REVIEWING|APPROVED|REJECTED)$",
            message = "材料状态不正确"
    )
    private String materialStatus;

    @Size(max = 128, message = "候选人关键词不能超过128个字符")
    @Schema(description = "候选人关键词，可匹配姓名、手机号或邮箱")
    private String candidateKeyword;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

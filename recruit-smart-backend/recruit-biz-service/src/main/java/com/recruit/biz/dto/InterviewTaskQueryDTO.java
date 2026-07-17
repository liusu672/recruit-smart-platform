package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "面试工作台任务查询参数")
public class InterviewTaskQueryDTO {

    @Schema(description = "候选人、职位或面试官关键词")
    @Size(max = 128, message = "关键词不能超过128个字符")
    private String keyword;

    @Schema(description = "面试状态", example = "SCHEDULED")
    @Pattern(
            regexp = "^(SCHEDULED|COMPLETED|CANCELED|REINTERVIEW)$",
            message = "面试状态不正确"
    )
    private String status;

    @Schema(description = "反馈状态", example = "EMPTY")
    @Pattern(
            regexp = "^(EMPTY|DRAFT|SUBMITTED)$",
            message = "反馈状态不正确"
    )
    private String feedbackState;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "员工离职风险分析数据")
public class EmployeeRiskDataUpdateDTO {

    @NotBlank(message = "绩效摘要不能为空")
    @Size(max = 4000, message = "绩效摘要不能超过4000个字符")
    @Schema(description = "本期绩效表现和关键任务完成情况")
    private String performanceSummary;

    @NotNull(message = "绩效评分不能为空")
    @Min(value = 0, message = "绩效评分不能小于0")
    @Max(value = 100, message = "绩效评分不能大于100")
    @Schema(description = "绩效评分，范围0到100", example = "78")
    private Integer performanceScore;

    @NotBlank(message = "考勤摘要不能为空")
    @Size(max = 4000, message = "考勤摘要不能超过4000个字符")
    @Schema(description = "考勤表现和异常情况")
    private String attendanceSummary;

    @NotNull(message = "考勤评分不能为空")
    @Min(value = 0, message = "考勤评分不能小于0")
    @Max(value = 100, message = "考勤评分不能大于100")
    @Schema(description = "考勤评分，范围0到100", example = "92")
    private Integer attendanceScore;

    @NotBlank(message = "满意度反馈不能为空")
    @Size(max = 4000, message = "满意度反馈不能超过4000个字符")
    @Schema(description = "员工满意度或访谈反馈")
    private String satisfactionFeedback;

    @NotNull(message = "满意度评分不能为空")
    @Min(value = 0, message = "满意度评分不能小于0")
    @Max(value = 100, message = "满意度评分不能大于100")
    @Schema(description = "满意度评分，范围0到100", example = "85")
    private Integer satisfactionScore;
}

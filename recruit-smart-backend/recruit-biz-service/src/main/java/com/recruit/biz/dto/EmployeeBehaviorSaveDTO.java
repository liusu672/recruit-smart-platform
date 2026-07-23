package com.recruit.biz.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeBehaviorSaveDTO {

    @NotNull(message = "周期开始日期不能为空")
    private LocalDate periodStart;

    @NotNull(message = "周期结束日期不能为空")
    private LocalDate periodEnd;

    @Min(value = 0, message = "绩效评分不能小于0")
    @Max(value = 100, message = "绩效评分不能大于100")
    private Integer performanceScore;

    @Size(max = 2000, message = "绩效摘要不能超过2000个字符")
    private String performanceSummary;

    @DecimalMin(value = "0.00", message = "任务完成率不能小于0")
    @DecimalMax(value = "100.00", message = "任务完成率不能大于100")
    private BigDecimal taskCompletionRate;

    @Min(value = 0, message = "迟到次数不能小于0")
    private Integer lateCount;

    @DecimalMin(value = "0.0", message = "缺勤天数不能小于0")
    private BigDecimal absenceDays;

    @DecimalMin(value = "0.0", message = "请假天数不能小于0")
    private BigDecimal leaveDays;

    @DecimalMin(value = "0.0", message = "加班小时数不能小于0")
    private BigDecimal overtimeHours;

    @Min(value = 0, message = "考勤评分不能小于0")
    @Max(value = 100, message = "考勤评分不能大于100")
    private Integer attendanceScore;

    @Size(max = 2000, message = "考勤摘要不能超过2000个字符")
    private String attendanceSummary;

    @Min(value = 0, message = "满意度评分不能小于0")
    @Max(value = 100, message = "满意度评分不能大于100")
    private Integer satisfactionScore;

    @Size(max = 5000, message = "反馈文本不能超过5000个字符")
    private String feedbackText;

    @Pattern(
            regexp = "HR_INPUT|IMPORT|SYSTEM",
            message = "行为数据来源不正确"
    )
    private String sourceType;
}
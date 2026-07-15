package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "本人面试列表查询参数")
public class InterviewQueryDTO {

    @Schema(description = "面试状态", example = "SCHEDULED")
    @Pattern(
            regexp = "^(SCHEDULED|COMPLETED|CANCELED|REINTERVIEW)$",
            message = "面试状态不正确"
    )
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "面试开始时间", example = "2026-07-15T00:00:00")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "面试结束时间", example = "2026-07-31T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "面试官预约并确认面试参数")
public class InterviewScheduleDTO {

    @NotNull(message = "面试时间不能为空")
    @Future(message = "面试时间必须晚于当前时间")
    @Schema(description = "面试时间", example = "2026-07-20T14:00:00")
    private LocalDateTime interviewTime;

    @NotBlank(message = "面试方式不能为空")
    @Pattern(
            regexp = "^(ONLINE|OFFLINE|PHONE)$",
            message = "面试方式不正确"
    )
    @Schema(description = "面试方式", example = "ONLINE")
    private String method;

    @NotBlank(message = "面试地点或会议链接不能为空")
    @Size(max = 255, message = "面试地点或会议链接不能超过255个字符")
    @Schema(description = "面试地点或会议链接")
    private String location;
}

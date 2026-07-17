package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "HR指派面试官参数")
public class InterviewCreateDTO {

    @NotNull(message = "投递记录不能为空")
    @Schema(description = "投递记录ID", example = "10")
    private Long applicationId;

    @NotNull(message = "面试官不能为空")
    @Schema(description = "面试官用户ID", example = "5")
    private Long interviewerId;

    @NotBlank(message = "面试轮次不能为空")
    @Pattern(
            regexp = "^(FIRST|SECOND|HR)$",
            message = "面试轮次不正确"
    )
    @Schema(description = "面试轮次", example = "FIRST")
    private String round;

}

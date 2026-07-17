package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "HR重新指派面试官参数")
public class InterviewUpdateDTO {

    @NotNull(message = "面试官不能为空")
    @Schema(description = "面试官用户ID", example = "5")
    private Long interviewerId;

}

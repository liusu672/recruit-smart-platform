package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改投递状态参数")
public class JobApplicationStatusUpdateDTO {

    @NotBlank(message = "投递状态不能为空")
    @Pattern(
            regexp = "^(SCREENING|SCREEN_PASSED|INTERVIEWING)$",
            message = "该状态不允许通过通用接口修改"
    )
    @Schema(description = "目标投递状态", example = "INTERVIEWING")
    private String status;
}

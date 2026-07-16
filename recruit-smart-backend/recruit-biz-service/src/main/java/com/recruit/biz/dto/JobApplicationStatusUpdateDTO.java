package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "开始筛选参数")
public class JobApplicationStatusUpdateDTO {

    @NotBlank(message = "投递状态不能为空")
    @Pattern(
            regexp = "^SCREENING$",
            message = "通用状态接口仅用于开始筛选"
    )
    @Schema(description = "目标投递状态", example = "SCREENING")
    private String status;
}

package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改员工状态参数")
public class EmployeeStatusUpdateDTO {

    @NotBlank(message = "员工状态不能为空")
    @Pattern(
            regexp = "^(PROBATION|ACTIVE|LEFT)$",
            message = "员工状态不正确"
    )
    @Schema(description = "目标员工状态", example = "ACTIVE")
    private String status;
}

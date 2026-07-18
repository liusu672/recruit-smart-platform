package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员修改用户状态参数")
public class AdminUserStatusUpdateDTO {

    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态不正确")
    @Max(value = 1, message = "账号状态不正确")
    @Schema(description = "账号状态：1启用，0禁用")
    private Integer status;
}

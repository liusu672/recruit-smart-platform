package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员修改用户角色参数")
public class AdminUserRoleUpdateDTO {

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色ID")
    private Long roleId;
}

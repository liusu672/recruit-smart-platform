package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "管理员用户查询参数")
public class AdminUserQueryDTO {

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    @Schema(description = "用户名、姓名、手机号或邮箱关键词")
    private String keyword;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "账号状态：1启用，0禁用")
    @Min(value = 0, message = "账号状态不正确")
    @Max(value = 1, message = "账号状态不正确")
    private Integer status;
}

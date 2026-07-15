package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "员工档案分页查询参数")
public class EmployeeQueryDTO {

    @Size(max = 128, message = "搜索关键词不能超过128个字符")
    @Schema(description = "关键词，可匹配员工编号、姓名、手机号或邮箱")
    private String keyword;

    @Size(max = 128, message = "部门名称不能超过128个字符")
    @Schema(description = "部门", example = "研发部")
    private String department;

    @Schema(description = "员工状态", example = "ACTIVE")
    @Pattern(
            regexp = "^(PROBATION|ACTIVE|LEFT)$",
            message = "员工状态不正确"
    )
    private String status;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

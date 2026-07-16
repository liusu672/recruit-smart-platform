package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "职位分页查询参数")
public class JobPositionQueryDTO {

    @Schema(description = "搜索关键词，按职位名称模糊查询", example = "Java")
    private String keyword;

    @Schema(description = "所属部门", example = "技术部")
    private String department;

    @Schema(description = "职位状态：DRAFT草稿、OPEN招聘中、PAUSED已暂停、CLOSED已关闭", example = "OPEN")
    @Pattern(
            regexp = "^(DRAFT|OPEN|PAUSED|CLOSED)$",
            message = "职位状态不正确"
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

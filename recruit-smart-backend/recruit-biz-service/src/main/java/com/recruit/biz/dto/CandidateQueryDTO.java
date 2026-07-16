package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "候选人查询参数")
public class CandidateQueryDTO {
    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;
    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    /**
     * 查询姓名、手机号、邮箱
     */
    @Schema(description = "搜索关键词，按关键词模糊查询", example = "吴家乐")
    private String keyword;
    @Schema(description = "性别")
    private String gender;
    @Schema(description = "学历")
    private String education;
    @Schema(description = "学校")
    private String school;
    @Schema(description = "专业")
    private String major;
    @Schema(description = "当前状态")
    private String currentStatus;
    @Schema(description = "来源")
    private String source;
    @Schema(description = "最低工作时间要求")
    @Min(value = 0, message = "最低工作年限不能小于0")
    private Integer minYearsOfExperience;
    @Schema(description = "最高工作时间要求")
    @Min(value = 0, message = "最高工作年限不能小于0")
    private Integer maxYearsOfExperience;
}

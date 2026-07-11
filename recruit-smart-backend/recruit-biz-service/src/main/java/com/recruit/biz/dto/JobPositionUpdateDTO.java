package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "修改职位请求参数")
public class JobPositionUpdateDTO {
    @Schema(description = "职位名称", example = "Java后端开发工程师")
    @NotBlank(message = "职位名称不能为空")
    private String title;
    @Schema(description = "所属部门", example = "技术部")
    @NotBlank(message = "部门不能为空")
    private String department;

    @Schema(description = "所在地址", example = "武汉")
    private String location;

    @Schema(description = "最低薪资", example = "9000")
    @NotNull(message = "最低薪资不能为空")
    private BigDecimal salaryMin;

    @Schema(description = "最高薪资", example = "15000")
    @NotNull(message = "最高薪资不能为空")
    private BigDecimal salaryMax;
    @Schema(description = "所需人数", example = "1")
    @NotNull(message = "人数需求不能为空")
    @Min(value = 1, message = "最少需要招收1人")
    private Integer headcount;
    @Schema(description = "岗位职责", example = "负责招聘平台后端接口开发、业务流程实现和数据库设计。")
    private String responsibilities;
    @Schema(description = "任职要求", example = "熟悉Java、Spring Boot、MySQL，了解微服务架构，有项目经验优先。")
    private String requirements;
}
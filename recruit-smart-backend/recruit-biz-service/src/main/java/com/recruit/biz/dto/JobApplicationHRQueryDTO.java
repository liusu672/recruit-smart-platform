package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "HR按职位查询投递参数")
public class JobApplicationHRQueryDTO {

    @Schema(description = "投递状态", example = "SUBMITTED")
    @Pattern(
            regexp = "^(SUBMITTED|SCREENING|SCREEN_PASSED|INTERVIEWING|OFFERED|HIRED|SCREEN_REJECT|REJECTED|WITHDRAWN)$",
            message = "投递状态不正确"
    )
    private String status;

    @Schema(description = "候选人关键词，可匹配姓名、手机号或邮箱", example = "张三")
    private String candidateKeyword;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

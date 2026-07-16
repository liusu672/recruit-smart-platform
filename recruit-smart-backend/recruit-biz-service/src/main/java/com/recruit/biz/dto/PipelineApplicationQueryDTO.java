package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "招聘流程看板查询参数")
public class PipelineApplicationQueryDTO {

    @Schema(description = "投递状态", example = "INTERVIEWING")
    @Pattern(
            regexp = "^(SUBMITTED|SCREENING|SCREEN_PASSED|INTERVIEWING|OFFERED|HIRED|SCREEN_REJECT|REJECTED|WITHDRAWN)$",
            message = "投递状态不正确"
    )
    private String status;

    @Schema(description = "职位ID")
    @Min(value = 1, message = "职位ID必须大于0")
    private Long jobId;

    @Schema(description = "候选人、职位或部门关键词")
    @Size(max = 128, message = "关键词不能超过128个字符")
    private String keyword;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

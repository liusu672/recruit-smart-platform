package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "职位投递参数")
public class JobApplicationCreateDTO {

    @NotNull(message = "职位不能为空")
    @Schema(description = "职位ID", example = "1")
    private Long jobId;

    @NotNull(message = "简历不能为空")
    @Schema(description = "简历ID", example = "2")
    private Long resumeId;

    @Schema(description = "是否接受岗位调剂", example = "false")
    private Boolean allowAdjustment;
}

package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "拒绝投递参数")
public class JobApplicationRejectDTO {

    @NotBlank(message = "拒绝原因编码不能为空")
    @Size(max = 64, message = "拒绝原因编码不能超过64个字符")
    @Schema(description = "拒绝原因编码", example = "SKILL_NOT_MATCH")
    private String reasonCode;

    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 500, message = "拒绝原因不能超过500个字符")
    @Schema(description = "拒绝原因说明", example = "项目经验与岗位要求不匹配")
    private String reason;
}

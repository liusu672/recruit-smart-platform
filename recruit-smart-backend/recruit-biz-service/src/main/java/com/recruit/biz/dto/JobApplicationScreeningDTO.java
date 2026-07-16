package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "提交简历筛选结论")
public class JobApplicationScreeningDTO {

    @NotBlank(message = "筛选结论不能为空")
    @Pattern(
            regexp = "^(PASS|REJECT|PENDING)$",
            message = "筛选结论只能是PASS、REJECT或PENDING"
    )
    @Schema(description = "筛选结论", example = "PASS")
    private String decision;

    @Size(max = 64, message = "拒绝原因编码不能超过64个字符")
    @Schema(description = "拒绝原因编码", example = "SKILL_NOT_MATCH")
    private String rejectReasonCode;

    @Size(max = 500, message = "筛选备注不能超过500个字符")
    @Schema(description = "筛选备注或待核实事项")
    private String note;
}

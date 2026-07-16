package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "驳回入职材料参数")
public class OnboardingMaterialRejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因不能超过500个字符")
    @Schema(description = "驳回原因", example = "学历证明信息不清晰，请重新提交")
    private String reason;
}

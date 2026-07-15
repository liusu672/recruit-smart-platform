package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "简历修改名称参数")
public class ResumeRenameDTO {
    @NotBlank(message = "简历名称不能为空")
    @Size(max = 128, message = "简历名称不能超过128个字符")
    @Schema(description = "新的简历名称", example = "Java 微服务开发简历")
    private String resumeName;
}

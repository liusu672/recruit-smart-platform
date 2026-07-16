package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "简历上传参数")
public class ResumeUploadDTO {
    @NotBlank(message = "简历名称不能为空")
    @Size(max=128,message = "简历名称不能超过128个字符")
    @Schema(description = "简历名称",example = "吴家乐超级无敌牛逼小简历")
    private String resumeName;
    @NotNull(message = "简历文件不能为空")
    @Schema(description = "简历文件",type="string",format = "binary")
    private MultipartFile file;
    @Schema(description = "是否设置为默认简历", example = "true")
    private Boolean setDefault;
}

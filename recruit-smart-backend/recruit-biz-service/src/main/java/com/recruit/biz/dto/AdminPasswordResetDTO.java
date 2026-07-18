package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员重置用户密码参数")
public class AdminPasswordResetDTO {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度为6到32位")
    @Schema(description = "新密码")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码")
    private String confirmPassword;
}

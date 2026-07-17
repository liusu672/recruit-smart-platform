package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "发送站内消息参数")
public class MessageSendDTO {
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容不能超过5000个字符")
    @Schema(description = "消息正文", example = "请确认明天下午的面试时间。")
    private String content;
}

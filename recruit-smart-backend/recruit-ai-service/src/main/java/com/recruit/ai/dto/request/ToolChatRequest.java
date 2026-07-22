package com.recruit.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ToolChatRequest {
    @NotBlank(message = "消息不能为空")
    @Size(max = 1000, message = "消息不能超过1000个字符")
    private String message;
}

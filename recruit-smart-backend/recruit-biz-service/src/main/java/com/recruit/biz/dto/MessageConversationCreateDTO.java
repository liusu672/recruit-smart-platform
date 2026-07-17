package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建或获取投递消息会话参数")
public class MessageConversationCreateDTO {
    @NotNull(message = "投递记录不能为空")
    @Schema(description = "投递记录ID", example = "1")
    private Long applicationId;
}

package com.recruit.ai.service.impl;

import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.prompt.ToolChatPrompts;
import com.recruit.ai.service.ToolChatService;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolChatServiceImpl implements ToolChatService {

    private final ChatClient chatClient;

    @Override
    public String chat(ToolChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "消息不能为空");
        }

        String content;
        try {
            content = chatClient.prompt()
                    .system(ToolChatPrompts.SYSTEM_PROMPT)
                    .user(request.getMessage().trim())
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            log.error("AI Tool Chat调用失败", exception);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "AI 工具调用失败，请稍后重试");
        }

        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "AI 工具调用未返回有效内容");
        }

        return content;
    }
}

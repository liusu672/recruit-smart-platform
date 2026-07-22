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
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolChatServiceImpl
        implements ToolChatService {

    private final ChatClient chatClient;

    @Override
    public String chat(ToolChatRequest request) {
        validateRequest(request);

        try {
            String content = chatClient.prompt()
                    .system(ToolChatPrompts.SYSTEM_PROMPT)
                    .user(request.getMessage().trim())
                    .call()
                    .content();

            if (!StringUtils.hasText(content)) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "AI工具调用未返回有效内容"
                );
            }

            return content;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("AI普通对话调用失败", exception);
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "AI工具调用失败，请稍后重试"
            );
        }
    }

    @Override
    public Flux<String> streamChat(
            ToolChatRequest request
    ) {
        validateRequest(request);

        try {
            return chatClient.prompt()
                    .system(ToolChatPrompts.SYSTEM_PROMPT)
                    .user(request.getMessage().trim())
                    /*
                     * AiToolConfig中的defaultTools会自动生效，
                     * 这里不需要重复传入工具。
                     */
                    .stream()
                    .content()
                    .filter(StringUtils::hasText)
                    .onErrorMap(exception -> {
                        log.error("AI流式对话调用失败", exception);

                        return new BusinessException(
                                ErrorCode.BUSINESS_ERROR,
                                "AI流式对话失败，请稍后重试"
                        );
                    });
        } catch (RuntimeException exception) {
            log.error("AI流式对话初始化失败", exception);

            return Flux.error(
                    new BusinessException(
                            ErrorCode.BUSINESS_ERROR,
                            "AI流式对话初始化失败"
                    )
            );
        }
    }

    private void validateRequest(
            ToolChatRequest request
    ) {
        if (request == null
                || !StringUtils.hasText(request.getMessage())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "消息不能为空"
            );
        }

        if (request.getMessage().length() > 1000) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "消息不能超过1000个字符"
            );
        }
    }
}
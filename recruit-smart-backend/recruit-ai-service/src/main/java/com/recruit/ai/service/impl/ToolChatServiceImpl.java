package com.recruit.ai.service.impl;

import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.prompt.ToolChatPrompts;
import com.recruit.ai.service.ToolChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolChatServiceImpl implements ToolChatService {

    private final ChatClient chatClient;

    @Override
    public String chat(ToolChatRequest request) {
        return chatClient.prompt()
                .system(ToolChatPrompts.SYSTEM_PROMPT)
                .user(request.getMessage())
                .call()
                .content();
    }
}
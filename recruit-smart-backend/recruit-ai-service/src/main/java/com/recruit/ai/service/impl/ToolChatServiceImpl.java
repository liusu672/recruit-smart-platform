package com.recruit.ai.service.impl;

import com.recruit.ai.dto.request.ToolChatRequest;
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
                .system("""
                        你是招聘与人才管理平台的AI助手。
                        你可以在需要时调用工具获取当前日期时间，也可以查询AI任务记录。
                        查询数据库时，只能返回任务相关信息，不要编造不存在的数据。
                        """)
                .user(request.getMessage())
                .call()
                .content();
    }
}
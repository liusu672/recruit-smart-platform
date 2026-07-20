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
                        你可以在需要时调用工具获取当前日期时间、查询AI任务记录、查询AI简历匹配结果。
                        当用户询问最近AI任务、任务详情、简历匹配结果、低匹配候选人、面试题生成结果、面试反馈摘要、离职风险检测时，应优先调用工具查询真实数据库。
                        只能基于工具返回的数据回答，不要编造不存在的数据。
                        不要返回候选人手机号、身份证号、完整简历等敏感信息。
                        """)
                .user(request.getMessage())
                .call()
                .content();
    }
}
package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import com.recruit.ai.prompt.ResumeMatchPrompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmResumeMatchService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public ResumeMatchResponse match(ResumeMatchRequest request, String knowledgeContext) {
        try {
            String content = chatClientBuilder.build()
                    .prompt()
                    .system(ResumeMatchPrompts.SYSTEM_PROMPT)
                    .user(ResumeMatchPrompts.buildUserPrompt(request, knowledgeContext))
                    .call()
                    .content();

            String json = extractJson(content);
            return objectMapper.readValue(json, ResumeMatchResponse.class);
        } catch (Exception e) {
            log.error("大模型简历匹配失败", e);
            throw new RuntimeException("大模型简历匹配失败: " + e.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("大模型返回内容为空");
        }

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("大模型返回内容不是 JSON: " + content);
        }

        return content.substring(start, end + 1);
    }
}
package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.response.ResumeParseResponse;
import com.recruit.ai.prompt.ResumeParsePrompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmResumeParseService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public ResumeParseResponse extract(String resumeText) {
        try {
            String content = chatClientBuilder.build()
                    .prompt()
                    .system(ResumeParsePrompts.SYSTEM_PROMPT)
                    .user(ResumeParsePrompts.buildUserPrompt(resumeText))
                    .call()
                    .content();

            return objectMapper.readValue(
                    extractJson(content),
                    ResumeParseResponse.class
            );
        } catch (Exception e) {
            log.error("大模型简历结构化解析失败", e);
            throw new IllegalStateException(
                    "大模型简历结构化解析失败",
                    e
            );
        }
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("大模型返回内容为空");
        }

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("大模型返回内容不是JSON");
        }
        return content.substring(start, end + 1);
    }
}

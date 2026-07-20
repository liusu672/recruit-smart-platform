package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import com.recruit.ai.prompt.InterviewQuestionPrompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmInterviewQuestionService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public InterviewQuestionResponse generate(InterviewQuestionRequest request, String knowledgeContext) {
        String systemPrompt = """
                你是招聘与人才管理平台的 AI 面试题生成助手。

                你的任务：
                根据岗位信息、候选人简历和知识库参考内容，生成适合面试官使用的面试问题。

                规则：
                1. 必须结合岗位要求和候选人经历生成问题。
                2. 知识库内容只是参考，不是硬性要求。
                3. 问题要适合真实面试场景，避免空泛。
                4. 优先生成技术能力、项目经验、岗位匹配、综合素质相关问题。
                5. 不要生成与岗位无关的问题。
                6. 必须只返回 JSON，不要返回 Markdown，不要解释。

                JSON 格式：
                {
                  "category": "Java后端",
                  "summary": "已根据岗位要求和候选人简历生成推荐面试题。",
                  "questions": [
                    "问题1",
                    "问题2"
                  ]
                }
                """;

        String userPrompt = """
                【岗位信息】
                岗位名称：%s
                岗位职责：%s
                岗位要求：%s

                【候选人简历】
                简历正文：%s
                技能：%s
                项目经验：%s
                工作经验：%s

                【知识库参考内容】
                %s

                请生成 8 道面试题，返回 JSON。
                """.formatted(
                safe(request.getJobTitle()),
                safe(request.getResponsibilities()),
                safe(request.getRequirements()),
                safe(request.getResumeText()),
                safe(request.getSkills()),
                safe(request.getProjectExperience()),
                safe(request.getWorkExperience()),
                safe(knowledgeContext)
        );

        try {
            String content = chatClientBuilder.build()
                    .prompt()
                    .system(InterviewQuestionPrompts.SYSTEM_PROMPT)
                    .user(InterviewQuestionPrompts.buildUserPrompt(request, knowledgeContext))
                    .call()
                    .content();

            String json = extractJson(content);
            return objectMapper.readValue(json, InterviewQuestionResponse.class);
        } catch (Exception e) {
            log.error("大模型面试题生成失败", e);
            throw new RuntimeException("大模型面试题生成失败: " + e.getMessage());
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
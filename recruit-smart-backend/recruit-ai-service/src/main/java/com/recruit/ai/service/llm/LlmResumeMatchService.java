package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
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
        String systemPrompt = """
                你是招聘与人才管理平台的 AI 简历匹配助手。

                你的任务：
                根据岗位信息、候选人简历、知识库参考内容，判断候选人与岗位的匹配程度。

                规则：
                1. 必须进行语义理解，不要只做关键词匹配。
                2. 知识库内容只是参考，不是硬性岗位要求。
                3. 不要把 Offer、入职办理、员工档案、面试安排等流程词当作技能。
                4. AI 结果只作为 HR 参考，不能表示最终录用决定。
                5. 必须只返回 JSON，不要返回 Markdown，不要解释。

                JSON 格式：
                {
                  "score": 0,
                  "level": "LOW",
                  "summary": "",
                  "matchedPoints": [],
                  "riskPoints": [],
                  "suggestion": ""
                }

                level 规则：
                score >= 80 为 HIGH
                score >= 60 为 MEDIUM
                score < 60 为 LOW
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

                请完成简历岗位匹配分析，并返回 JSON。
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
                    .system(systemPrompt)
                    .user(userPrompt)
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
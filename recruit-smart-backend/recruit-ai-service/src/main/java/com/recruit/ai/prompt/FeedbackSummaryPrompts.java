package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;

public final class FeedbackSummaryPrompts {

    public static final String VERSION = "feedback-summary-v1";

    public static final String SYSTEM_PROMPT = """
            你是招聘与人才管理平台的 AI 面试反馈摘要助手。

            你的任务：
            根据面试官反馈、岗位信息、候选人信息和知识库参考内容，生成结构化面试反馈摘要。

            规则：
            1. 必须基于面试反馈文本总结，不要凭空编造。
            2. 知识库内容只是评价标准参考，不是硬性结论。
            3. 摘要要客观、中立，不能直接决定录用结果。
            4. advantages 只放候选人优势。
            5. risks 只放不足、风险点或需要进一步确认的问题。
            6. suggestion 给 HR 参考建议。
            7. 必须只返回 JSON，不要返回 Markdown，不要解释。

            JSON 格式：
            {
              "summary": "整体摘要",
              "advantages": [],
              "risks": [],
              "suggestion": "建议"
            }
            """;

    public static String buildUserPrompt(FeedbackSummaryRequest request, String knowledgeContext) {
        return """
                【岗位信息】
                岗位名称：%s

                【候选人信息】
                候选人姓名：%s
                面试评分：%s

                【面试官反馈原文】
                %s

                【知识库参考内容】
                %s

                请生成面试反馈结构化摘要，并返回 JSON。
                """.formatted(
                safe(request.getJobTitle()),
                safe(request.getCandidateName()),
                request.getScore() == null ? "未提供" : request.getScore().toString(),
                safe(request.getFeedbackText()),
                safe(knowledgeContext)
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private FeedbackSummaryPrompts() {
    }
}
package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.feign.dto.request.FeedbackScoreItemRequest;

import java.util.List;

public final class FeedbackSummaryPrompts {

    public static final String VERSION = "feedback-summary-v2";

    public static final String SYSTEM_PROMPT = """
            你是招聘与人才管理平台的 AI 面试反馈摘要助手。

            你的任务：
            根据本轮面试的结构化评分卡、面试官评价、岗位信息和候选人信息，
            生成可追溯的结构化面试反馈摘要。

            规则：
            1. 结构化评分卡中的“面试官依据”是判断优势和风险的首要事实来源。
            2. 综合评价只能作为补充，不能覆盖或歪曲评分卡中的具体依据。
            3. 评分为 3-4 分的维度可归纳为优势，评分为 1-2 分的维度应归纳为风险。
            4. 每条优势和风险必须对应实际评价依据；没有依据时应明确写“待核实”，不得编造。
            5. 不得仅凭 PASS、录用建议或总分推断候选人的具体能力。
            6. 摘要必须明确反映本次面试轮次，不得混用其他轮次的结论。
            7. 知识库内容只是评价标准参考，不是候选人的事实。
            8. 摘要要客观、中立，不能直接替代 HR 的录用决定。
            9. suggestion 应结合评分卡事实和面试官录用建议，给出 HR 参考建议。
            10. 必须只返回 JSON，不要返回 Markdown，不要解释。

            JSON 格式：
            {
              "summary": "整体摘要",
              "advantages": [],
              "risks": [],
              "suggestion": "建议"
            }
            """;

    public static String buildUserPrompt(
            FeedbackSummaryRequest request,
            String knowledgeContext
    ) {
        return """
                【岗位信息】
                岗位名称：%s

                【候选人及本轮面试】
                候选人姓名：%s
                面试轮次：%s
                面试总分：%s
                面试官录用建议：%s

                【本轮结构化评分卡】
                %s

                【面试官综合评价】
                %s

                【知识库参考内容】
                %s

                请严格以本轮评分卡的评分和评价依据为主生成摘要，并返回 JSON。
                """.formatted(
                safe(request.getJobTitle()),
                safe(request.getCandidateName()),
                safe(request.getInterviewRound()),
                request.getScore() == null
                        ? "未提供"
                        : request.getScore().toString(),
                safe(request.getSuggestion()),
                formatScorecard(request.getScorecard()),
                safe(request.getFeedbackText()),
                safe(knowledgeContext)
        );
    }

    private static String formatScorecard(
            List<FeedbackScoreItemRequest> scorecard
    ) {
        if (scorecard == null || scorecard.isEmpty()) {
            return "未提供";
        }

        StringBuilder builder = new StringBuilder();
        for (FeedbackScoreItemRequest item : scorecard) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }
            builder.append("- ")
                    .append(safe(item.getLabel()))
                    .append("：")
                    .append(item.getScore() == null
                            ? "未评分"
                            : item.getScore() + "/4");
            if (hasText(item.getDescription())) {
                builder.append("\n  评价标准：")
                        .append(item.getDescription().trim());
            }
            builder.append("\n  面试官依据：")
                    .append(hasText(item.getEvidence())
                            ? item.getEvidence().trim()
                            : "未提供，待核实");
        }
        return builder.toString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private FeedbackSummaryPrompts() {
    }
}

package com.recruit.ai.algorithm.summary;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import com.recruit.feign.dto.request.FeedbackScoreItemRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class FeedbackSummaryAlgorithm {

    private static final int MAX_FINDINGS = 3;

    public FeedbackSummaryResponse generate(FeedbackSummaryRequest request) {
        String feedbackText = safeText(request.getFeedbackText());
        List<FeedbackScoreItemRequest> scorecard =
                request.getScorecard() == null
                        ? List.of()
                        : request.getScorecard();

        List<String> advantages = extractScorecardFindings(
                scorecard,
                true
        );
        List<String> risks = extractScorecardFindings(
                scorecard,
                false
        );
        if (advantages.isEmpty()) {
            advantages = extractAdvantages(feedbackText);
        }
        if (risks.isEmpty()) {
            risks = extractRisks(feedbackText);
        }

        String suggestion = buildSuggestion(
                request.getScore(),
                request.getSuggestion(),
                advantages,
                risks
        );
        String summary = buildSummary(
                request.getCandidateName(),
                request.getJobTitle(),
                request.getInterviewRound(),
                advantages,
                risks,
                suggestion
        );

        FeedbackSummaryResponse response = new FeedbackSummaryResponse();
        response.setAdvantages(advantages);
        response.setRisks(risks);
        response.setSuggestion(suggestion);
        response.setSummary(summary);
        return response;
    }

    private List<String> extractScorecardFindings(
            List<FeedbackScoreItemRequest> scorecard,
            boolean advantage
    ) {
        return scorecard.stream()
                .filter(item -> item != null && item.getScore() != null)
                .filter(item -> advantage
                        ? item.getScore() >= 3
                        : item.getScore() <= 2)
                .map(this::formatScorecardFinding)
                .limit(MAX_FINDINGS)
                .toList();
    }

    private String formatScorecardFinding(FeedbackScoreItemRequest item) {
        String label = safeText(item.getLabel()).isEmpty()
                ? "未命名评分项"
                : item.getLabel().trim();
        String evidence = safeText(item.getEvidence()).isEmpty()
                ? "未提供具体评价依据，待核实"
                : item.getEvidence().trim();
        return label + "（" + item.getScore() + "/4）：" + evidence;
    }

    private List<String> extractAdvantages(String feedbackText) {
        List<String> advantages = new ArrayList<>();

        if (containsAny(feedbackText, "基础扎实", "基础较好", "基本功扎实")) {
            advantages.add("专业基础较扎实");
        }
        if (containsAny(feedbackText, "表达清晰", "沟通清晰", "表达流畅")) {
            advantages.add("沟通表达较清晰");
        }
        if (containsAny(
                feedbackText,
                "项目经验丰富",
                "项目经验较好",
                "做过项目"
        )) {
            advantages.add("具备一定项目经验");
        }
        if (containsAny(feedbackText, "思路清晰", "逻辑清晰", "分析清楚")) {
            advantages.add("分析思路较清晰");
        }
        if (containsAny(feedbackText, "学习能力强", "上手快", "学习能力较强")) {
            advantages.add("学习能力较强");
        }
        if (containsAny(feedbackText, "责任心强", "态度积极", "配合度高")) {
            advantages.add("工作态度较积极");
        }

        return advantages;
    }

    private List<String> extractRisks(String feedbackText) {
        List<String> risks = new ArrayList<>();

        if (containsAny(
                feedbackText,
                "项目深度不足",
                "项目理解不深",
                "项目经验不足"
        )) {
            risks.add("项目深度有待进一步确认");
        }
        if (containsAny(feedbackText, "缺乏经验", "经验不足", "实战经验不足")) {
            risks.add("相关经验不足");
        }
        if (containsAny(feedbackText, "回答不清晰", "表达不清晰", "回答模糊")) {
            risks.add("部分问题回答不够清晰");
        }
        if (containsAny(feedbackText, "基础薄弱", "基础一般", "基本功不足")) {
            risks.add("专业基础仍需加强");
        }
        if (containsAny(feedbackText, "沟通一般", "沟通较弱", "表达较弱")) {
            risks.add("沟通表达能力需要提升");
        }
        if (containsAny(
                feedbackText,
                "稳定性不足",
                "跳槽频繁",
                "职业规划不清晰"
        )) {
            risks.add("稳定性和职业规划需进一步评估");
        }

        return risks;
    }

    private String buildSuggestion(
            Integer score,
            String interviewerSuggestion,
            List<String> advantages,
            List<String> risks
    ) {
        String normalized = safeText(interviewerSuggestion)
                .toUpperCase(Locale.ROOT);
        if ("PASS".equals(normalized) || "PROCEED".equals(normalized)) {
            return risks.isEmpty()
                    ? "面试官建议通过；建议HR结合本轮评分依据推进后续流程。"
                    : "面试官建议通过；建议HR在推进前重点核实已识别的风险项。";
        }
        if ("REJECT".equals(normalized)) {
            return "面试官建议不通过；建议HR结合本轮评分依据复核后终止流程。";
        }
        if ("HOLD".equals(normalized)) {
            return "面试官建议待定；建议HR补充核实本轮风险项后再决定。";
        }

        if (score != null) {
            if (score >= 85) {
                return "建议结合本轮评分依据推进后续流程。";
            }
            if (score >= 60) {
                return "建议进入下一轮面试，并重点核实已识别的风险项。";
            }
            return "建议复核低分项后再决定是否继续推进。";
        }

        if (advantages.size() > risks.size()) {
            return "建议结合本轮评分依据评估是否进入下一轮面试。";
        }
        if (advantages.isEmpty() && risks.isEmpty()) {
            return "当前反馈信息不足，建议补充具体评分依据。";
        }
        return "建议核实风险项后再决定是否推进。";
    }

    private String buildSummary(
            String candidateName,
            String jobTitle,
            String interviewRound,
            List<String> advantages,
            List<String> risks,
            String suggestion
    ) {
        String name = safeText(candidateName).isEmpty()
                ? "候选人"
                : candidateName;
        String job = safeText(jobTitle).isEmpty()
                ? "当前岗位"
                : jobTitle;
        String round = safeText(interviewRound).isEmpty()
                ? "本轮面试"
                : interviewRound;

        String advantageText = advantages.isEmpty()
                ? "评分卡未提供可确认的明显优势"
                : String.join("；", advantages);
        String riskText = risks.isEmpty()
                ? "评分卡未提供可确认的明显风险点"
                : String.join("；", risks);

        return name + " 在 " + job + " 的" + round
                + "中，评分卡显示的优势为：" + advantageText
                + "；需要关注的风险为：" + riskText
                + "；综合建议：" + suggestion;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isBlank()) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }
}

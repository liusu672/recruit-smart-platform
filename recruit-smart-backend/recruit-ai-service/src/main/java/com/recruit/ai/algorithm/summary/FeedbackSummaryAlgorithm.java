package com.recruit.ai.algorithm.summary;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FeedbackSummaryAlgorithm {
    public FeedbackSummaryResponse generate(FeedbackSummaryRequest request) {
        String feedbackText = safeText(request.getFeedbackText());

        List<String> advantages = extractAdvantages(feedbackText);
        List<String> risks = extractRisks(feedbackText);
        String suggestion = buildSuggestion(request.getScore(), advantages, risks);
        String summary = buildSummary(
                request.getCandidateName(),
                request.getJobTitle(),
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

    private List<String> extractAdvantages(String feedbackText) {
        List<String> advantages = new ArrayList<>();

        if (containsAny(feedbackText, "基础扎实", "基础较好", "基本功扎实")) {
            advantages.add("专业基础较扎实");
        }
        if (containsAny(feedbackText, "表达清晰", "沟通清晰", "表达流畅")) {
            advantages.add("沟通表达较清晰");
        }
        if (containsAny(feedbackText, "项目经验丰富", "项目经验较好", "做过项目")) {
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

        if (containsAny(feedbackText, "项目深度不足", "项目理解不深", "项目经验不足")) {
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
        if (containsAny(feedbackText, "稳定性不足", "跳槽频繁", "职业规划不清晰")) {
            risks.add("稳定性和职业规划需进一步评估");
        }

        return risks;
    }

    private String buildSuggestion(Integer score, List<String> advantages, List<String> risks) {
        if (score != null) {
            if (score >= 85) {
                return "建议录用，可进入后续流程。";
            }
            if (score >= 60) {
                return "建议进入下一轮面试，重点核实风险点。";
            }
            return "暂不建议录用。";
        }

        if (advantages.size() > risks.size()) {
            return "建议进入下一轮面试。";
        }
        if (advantages.isEmpty() && risks.isEmpty()) {
            return "当前反馈信息不足，建议补充更详细的面试评价。";
        }
        return "建议谨慎评估后再决定是否推进。";
    }

    private String buildSummary(String candidateName,
                                String jobTitle,
                                List<String> advantages,
                                List<String> risks,
                                String suggestion) {
        String name = safeText(candidateName).isEmpty() ? "候选人" : candidateName;
        String job = safeText(jobTitle).isEmpty() ? "当前岗位" : jobTitle;

        String advantageText = advantages.isEmpty()
                ? "暂未识别出明显优势"
                : String.join("、", advantages);

        String riskText = risks.isEmpty()
                ? "暂未识别出明显风险点"
                : String.join("、", risks);

        return name + " 在 " + job + " 面试反馈中，表现出的优势包括：" + advantageText
                + "；需要关注的风险点包括：" + riskText
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

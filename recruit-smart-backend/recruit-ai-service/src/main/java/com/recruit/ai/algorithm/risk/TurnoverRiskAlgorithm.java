package com.recruit.ai.algorithm.risk;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TurnoverRiskAlgorithm {
    public TurnoverRiskResponse predict(
            TurnoverRiskRequest request
    ) {
        List<String> riskReasons = new ArrayList<>();
        int riskScore = 0;

        riskScore += evaluateScoreRisk(
                request.getPerformanceScore(),
                25,
                "最近一期绩效分数偏低",
                riskReasons
        );

        riskScore += evaluateScoreRisk(
                request.getAttendanceScore(),
                20,
                "最近一期考勤表现异常",
                riskReasons
        );

        riskScore += evaluateScoreRisk(
                request.getSatisfactionScore(),
                30,
                "最近一期满意度偏低",
                riskReasons
        );

        String text = joinText(
                request.getPerformanceSummary(),
                request.getAttendanceSummary(),
                request.getSatisfactionFeedback(),
                request.getLatestFeedback(),
                request.getPerformanceTrend(),
                request.getAttendanceTrend(),
                request.getSatisfactionTrend()
        );

        if (containsAny(
                text,
                "离职",
                "跳槽",
                "新机会",
                "换工作",
                "其他机会"
        )) {
            riskScore += 25;
            riskReasons.add("反馈中出现离职或跳槽倾向");
        }

        if (containsAny(
                text,
                "压力大",
                "工作压力",
                "不满意",
                "晋升",
                "薪资",
                "发展空间",
                "加班"
        )) {
            riskScore += 15;
            riskReasons.add("反馈中出现压力或发展诉求");
        }

        if (containsAny(
                text,
                "迟到",
                "缺勤",
                "考勤异常"
        )) {
            riskScore += 10;
            riskReasons.add("近期考勤表现出现异常");
        }

        if (containsAny(
                text,
                "下降",
                "延期增多",
                "积极性下降",
                "状态下降"
        )) {
            riskScore += 10;
            riskReasons.add("近期绩效或工作状态呈下降趋势");
        }

        riskScore = Math.min(riskScore, 100);

        String sentimentLabel = calculateSentimentLabel(
                request,
                text
        );

        int sentimentRiskScore = calculateSentimentRiskScore(
                request,
                text
        );

        String riskLevel = calculateRiskLevel(riskScore);

        TurnoverRiskResponse response = new TurnoverRiskResponse();
        response.setSentimentLabel(sentimentLabel);
        response.setSentimentRiskScore(sentimentRiskScore);
        response.setSentimentSummary(
                buildSentimentSummary(
                        sentimentLabel,
                        sentimentRiskScore
                )
        );
        response.setRiskLevel(riskLevel);
        response.setRiskScore(riskScore);
        response.setSummary(
                buildSummary(
                        request.getEmployeeName(),
                        request.getPosition(),
                        riskLevel,
                        riskScore,
                        riskReasons
                )
        );
        response.setRiskReasons(riskReasons);
        response.setSuggestions(buildSuggestions(riskLevel));

        return response;
    }

    private int evaluateScoreRisk(Integer score, int weight, String reason, List<String> riskReasons) {
        if (score != null && score < 60) {
            riskReasons.add(reason);
            return weight;
        }
        return 0;
    }

    private String calculateRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return "HIGH";
        }
        if (riskScore >= 40) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> buildSuggestions(String riskLevel) {
        List<String> suggestions = new ArrayList<>();

        switch (riskLevel) {
            case "HIGH" -> {
                suggestions.add("建议尽快安排 HR 或直属主管进行一对一沟通。");
                suggestions.add("重点了解员工对薪酬、工作压力、职业发展和团队氛围的真实感受。");
                suggestions.add("必要时制定岗位调整、成长计划或激励方案。");
            }
            case "MEDIUM" -> {
                suggestions.add("建议近期关注员工状态变化。");
                suggestions.add("可安排一次轻量沟通，了解员工当前诉求。");
                suggestions.add("结合绩效和考勤变化判断是否需要进一步干预。");
            }
            default -> {
                suggestions.add("当前离职风险较低，保持常规关注即可。");
                suggestions.add("建议继续定期跟进员工满意度和工作状态。");
            }
        }

        return suggestions;
    }

    private String buildSummary(String employeeName,
                                String position,
                                String riskLevel,
                                int riskScore,
                                List<String> riskReasons) {
        String name = safeText(employeeName).isEmpty() ? "该员工" : employeeName;
        String job = safeText(position).isEmpty() ? "当前岗位" : position;

        String reasonText = riskReasons.isEmpty()
                ? "暂未发现明显离职风险因素"
                : String.join("、", riskReasons);

        return name + " 在 " + job + " 的离职风险等级为 " + riskLevel
                + "，风险分为 " + riskScore
                + "。主要原因：" + reasonText + "。";
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null) {
                builder.append(value).append(" ");
            }
        }
        return builder.toString();
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

    private String calculateSentimentLabel(
            TurnoverRiskRequest request,
            String text
    ) {
        int score = calculateSentimentRiskScore(request, text);

        if (score >= 60) {
            return "NEGATIVE";
        }

        if (score >= 30) {
            return "NEUTRAL";
        }

        return "POSITIVE";
    }

    private int calculateSentimentRiskScore(
            TurnoverRiskRequest request,
            String text
    ) {
        int score = 0;

        Integer satisfactionScore =
                request.getSatisfactionScore();

        if (satisfactionScore != null) {
            if (satisfactionScore < 50) {
                score += 45;
            } else if (satisfactionScore < 70) {
                score += 30;
            } else if (satisfactionScore < 85) {
                score += 15;
            }
        }

        if (containsAny(
                text,
                "离职",
                "跳槽",
                "其他机会",
                "换工作"
        )) {
            score += 40;
        }

        if (containsAny(
                text,
                "压力大",
                "工作压力",
                "不满意",
                "晋升疑虑",
                "发展空间",
                "薪资"
        )) {
            score += 20;
        }

        if (containsAny(
                text,
                "满意",
                "稳定",
                "喜欢团队",
                "认可团队"
        )) {
            score -= 20;
        }

        return Math.max(0, Math.min(score, 100));
    }

    private String buildSentimentSummary(
            String label,
            int score
    ) {
        return switch (label) {
            case "NEGATIVE" ->
                    "反馈中存在较明显的负面情绪或离职倾向，情感风险分数为"
                            + score;
            case "NEUTRAL" ->
                    "反馈整体较为中性，存在一定压力或不确定因素，情感风险分数为"
                            + score;
            default ->
                    "反馈整体积极，暂未发现明显负面情绪，情感风险分数为"
                            + score;
        };
    }
}

package com.recruit.ai.algorithm.risk;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TurnoverRiskAlgorithm {
    public TurnoverRiskResponse predict(TurnoverRiskRequest request) {
        List<String> riskReasons = new ArrayList<>();
        int riskScore = 0;

        riskScore += evaluateScoreRisk(request.getPerformanceScore(), 25, "绩效评分偏低", riskReasons);
        riskScore += evaluateScoreRisk(request.getAttendanceScore(), 25, "考勤表现异常", riskReasons);
        riskScore += evaluateScoreRisk(request.getSatisfactionScore(), 30, "满意度评分偏低", riskReasons);

        String text = joinText(
                request.getPerformanceSummary(),
                request.getAttendanceSummary(),
                request.getSatisfactionFeedback(),
                request.getInterviewFeedback()
        );

        if (containsAny(text, "离职", "跳槽", "新机会", "换工作")) {
            riskScore += 20;
            riskReasons.add("反馈中出现离职倾向相关表达");
        }

        if (containsAny(text, "不满意", "压力大", "加班多", "发展空间小", "薪资低")) {
            riskScore += 20;
            riskReasons.add("反馈中出现满意度或发展诉求问题");
        }

        if (containsAny(text, "迟到", "缺勤", "请假频繁", "考勤异常")) {
            riskScore += 15;
            riskReasons.add("近期考勤表现存在异常");
        }

        if (containsAny(text, "绩效下降", "积极性下降", "状态不佳", "产出下降")) {
            riskScore += 15;
            riskReasons.add("近期工作状态或绩效表现下降");
        }

        riskScore = Math.min(riskScore, 100);
        String riskLevel = calculateRiskLevel(riskScore);
        List<String> suggestions = buildSuggestions(riskLevel);
        String summary = buildSummary(request.getEmployeeName(), request.getPosition(), riskLevel, riskScore, riskReasons);

        TurnoverRiskResponse response = new TurnoverRiskResponse();
        response.setRiskLevel(riskLevel);
        response.setRiskScore(riskScore);
        response.setRiskReasons(riskReasons);
        response.setSuggestions(suggestions);
        response.setSummary(summary);
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
}

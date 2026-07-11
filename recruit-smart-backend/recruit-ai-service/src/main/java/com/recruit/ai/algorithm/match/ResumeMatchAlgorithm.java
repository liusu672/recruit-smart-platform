package com.recruit.ai.algorithm.match;

import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResumeMatchAlgorithm {

    public ResumeMatchResponse match(ResumeMatchRequest request) {
        String jobText = joinText(
                request.getJobTitle(),
                request.getResponsibilities(),
                request.getRequirements()
        );

        String resumeText = joinText(
                request.getResumeText(),
                request.getSkills(),
                request.getProjectExperience(),
                request.getWorkExperience()
        );

        List<String> keywords = extractKeywords(jobText);

        /*
        单独处理关键词为空
         */
        ResumeMatchResponse response = new ResumeMatchResponse();
        if(keywords.isEmpty()){
            response.setScore(0);
            response.setLevel("LOW");
            response.setMatchedPoints(new ArrayList<>());
            response.setRiskPoints(List.of("未能从岗位信息中提取到有效关键词"));
            response.setSummary("岗位信息中的可识别关键词不足，当前无法完成有效匹配。");
            response.setSuggestion("请补充更明确的岗位要求，例如技术栈、项目经验、数据库、中间件等关键词。");
            return response;

        }

        List<String> matchedPoints = new ArrayList<>();

        for (String keyword : keywords) {
            if (resumeText.toLowerCase().contains(keyword.toLowerCase())) {
                matchedPoints.add(keyword);
            }
        }

        int score = calculateScore(keywords.size(), matchedPoints.size());
        String level = calculateLevel(score);

        response.setScore(score);
        response.setLevel(level);
        response.setMatchedPoints(matchedPoints);
        response.setRiskPoints(buildRiskPoints(keywords, matchedPoints));
        response.setSummary("候选人与岗位的匹配度为 " + score + " 分，推荐等级为 " + level + "。");
        response.setSuggestion(buildSuggestion(level));

        return response;
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

    private List<String> extractKeywords(String jobText) {
        List<String> baseKeywords = List.of(
                "Java", "Spring", "Spring Boot", "MySQL", "Redis", "微服务",
                "后端开发", "接口开发", "数据库", "项目经验",
                "Python", "机器学习", "深度学习", "RAG", "向量检索", "大模型",
                "招聘流程", "候选人沟通", "Offer", "入职办理"
        );

        List<String> result = new ArrayList<>();
        for (String keyword : baseKeywords) {
            if (jobText != null && jobText.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(keyword);
            }
        }
        return result;
    }

    private int calculateScore(int total, int matched) {
        if (total == 0) {
            return 0;
        }
        return Math.min(100, Math.round((matched * 100.0f) / total));
    }

    private String calculateLevel(int score) {
        if (score >= 80) {
            return "HIGH";
        }
        if (score >= 60) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> buildRiskPoints(List<String> keywords, List<String> matchedPoints) {
        List<String> risks = new ArrayList<>();
        for (String keyword : keywords) {
            if (!matchedPoints.contains(keyword)) {
                risks.add("缺少 " + keyword + " 相关经验");
            }
        }
        return risks;
    }

    private String buildSuggestion(String level) {
        return switch (level) {
            case "HIGH" -> "建议进入面试，重点确认项目深度和实际贡献。";
            case "MEDIUM" -> "可以进入备选池，建议进一步核实核心技能。";
            default -> "暂不建议优先推进，可作为低优先级候选人。";
        };
    }
}

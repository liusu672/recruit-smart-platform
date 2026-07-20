package com.recruit.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.ai.entity.AiMatchResult;
import com.recruit.ai.mapper.AiMatchResultMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiMatchResultQueryTools {

    private final AiMatchResultMapper aiMatchResultMapper;

    public AiMatchResultQueryTools(AiMatchResultMapper aiMatchResultMapper) {
        this.aiMatchResultMapper = aiMatchResultMapper;
    }

    @Tool(name = "queryRecentResumeMatchResults", description = "查询最近的AI简历匹配结果，可指定查询数量")
    public String queryRecentResumeMatchResults(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        try {
            int size = limit == null ? 5 : limit;
            size = Math.max(1, Math.min(size, 10));

            LambdaQueryWrapper<AiMatchResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AiMatchResult::getCreatedAt).last("limit " + size);

            List<AiMatchResult> results = aiMatchResultMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "当前没有AI简历匹配结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询AI简历匹配结果失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryResumeMatchResultByApplicationId", description = "根据投递记录ID查询AI简历匹配结果")
    public String queryResumeMatchResultByApplicationId(
            @ToolParam(description = "投递记录ID") Long applicationId
    ) {
        try {
            if (applicationId == null) {
                return "投递记录ID不能为空。";
            }

            LambdaQueryWrapper<AiMatchResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiMatchResult::getApplicationId, applicationId)
                    .orderByDesc(AiMatchResult::getCreatedAt)
                    .last("limit 1");

            AiMatchResult result = aiMatchResultMapper.selectOne(wrapper);

            if (result == null) {
                return "未查询到投递ID为 " + applicationId + " 的AI简历匹配结果。";
            }

            return formatResult(result);
        } catch (Exception e) {
            return "查询投递记录AI匹配结果失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryLowResumeMatchResults", description = "查询最近低匹配的AI简历匹配结果")
    public String queryLowResumeMatchResults(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        try {
            int size = limit == null ? 5 : limit;
            size = Math.max(1, Math.min(size, 10));

            LambdaQueryWrapper<AiMatchResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiMatchResult::getLevel, "LOW")
                    .orderByDesc(AiMatchResult::getCreatedAt)
                    .last("limit " + size);

            List<AiMatchResult> results = aiMatchResultMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "当前没有低匹配的AI简历匹配结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询低匹配AI简历结果失败：" + e.getMessage();
        }
    }

    private String formatResult(AiMatchResult result) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String generatedAt = result.getGeneratedAt() == null
                ? "-"
                : result.getGeneratedAt().format(formatter);

        return "结果ID：" + result.getId()
                + "，任务ID：" + result.getTaskId()
                + "，投递ID：" + result.getApplicationId()
                + "，岗位ID：" + result.getJobId()
                + "，候选人ID：" + result.getCandidateId()
                + "，简历ID：" + result.getResumeId()
                + "，匹配分数：" + result.getScore()
                + "，推荐等级：" + safe(result.getLevel())
                + "，结果来源：" + safe(result.getSource())
                + "，摘要：" + safe(result.getSummary())
                + "，建议：" + safe(result.getSuggestion())
                + "，生成时间：" + generatedAt;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
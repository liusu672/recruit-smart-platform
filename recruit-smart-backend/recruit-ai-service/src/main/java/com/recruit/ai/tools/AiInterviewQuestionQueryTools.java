package com.recruit.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.ai.entity.AiInterviewQuestion;
import com.recruit.ai.mapper.AiInterviewQuestionMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiInterviewQuestionQueryTools {

    private final AiInterviewQuestionMapper aiInterviewQuestionMapper;

    public AiInterviewQuestionQueryTools(AiInterviewQuestionMapper aiInterviewQuestionMapper) {
        this.aiInterviewQuestionMapper = aiInterviewQuestionMapper;
    }

    @Tool(name = "queryRecentInterviewQuestionResults", description = "查询最近的AI面试题生成结果，可指定查询数量")
    public String queryRecentInterviewQuestionResults(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        try {
            int size = limit == null ? 5 : limit;
            size = Math.max(1, Math.min(size, 10));

            LambdaQueryWrapper<AiInterviewQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AiInterviewQuestion::getCreatedAt).last("limit " + size);

            List<AiInterviewQuestion> results = aiInterviewQuestionMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "当前没有AI面试题生成结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询AI面试题生成结果失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryInterviewQuestionResultsByJobId", description = "根据岗位ID查询AI面试题生成结果")
    public String queryInterviewQuestionResultsByJobId(
            @ToolParam(description = "岗位ID") Long jobId
    ) {
        try {
            if (jobId == null) {
                return "岗位ID不能为空。";
            }

            LambdaQueryWrapper<AiInterviewQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiInterviewQuestion::getJobId, jobId)
                    .orderByDesc(AiInterviewQuestion::getCreatedAt)
                    .last("limit 5");

            List<AiInterviewQuestion> results = aiInterviewQuestionMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "未查询到岗位ID为 " + jobId + " 的AI面试题生成结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "根据岗位ID查询AI面试题失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryInterviewQuestionResultsByCandidateId", description = "根据候选人ID查询AI面试题生成结果")
    public String queryInterviewQuestionResultsByCandidateId(
            @ToolParam(description = "候选人ID") Long candidateId
    ) {
        try {
            if (candidateId == null) {
                return "候选人ID不能为空。";
            }

            LambdaQueryWrapper<AiInterviewQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiInterviewQuestion::getCandidateId, candidateId)
                    .orderByDesc(AiInterviewQuestion::getCreatedAt)
                    .last("limit 5");

            List<AiInterviewQuestion> results = aiInterviewQuestionMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "未查询到候选人ID为 " + candidateId + " 的AI面试题生成结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "根据候选人ID查询AI面试题失败：" + e.getMessage();
        }
    }

    private String formatResult(AiInterviewQuestion result) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String generatedAt = result.getGeneratedAt() == null
                ? "-"
                : result.getGeneratedAt().format(formatter);

        return "结果ID：" + result.getId()
                + "，任务ID：" + result.getTaskId()
                + "，岗位ID：" + result.getJobId()
                + "，候选人ID：" + result.getCandidateId()
                + "，简历ID：" + result.getResumeId()
                + "，类别：" + safe(result.getCategory())
                + "，摘要：" + safe(result.getSummary())
                + "，题目列表：" + safe(result.getQuestions())
                + "，来源：" + safe(result.getSource())
                + "，生成时间：" + generatedAt;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
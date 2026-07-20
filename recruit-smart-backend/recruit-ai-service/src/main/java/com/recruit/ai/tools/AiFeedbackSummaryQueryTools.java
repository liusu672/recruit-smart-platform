package com.recruit.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.ai.entity.AiFeedbackSummary;
import com.recruit.ai.mapper.AiFeedbackSummaryMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiFeedbackSummaryQueryTools {

    private final AiFeedbackSummaryMapper aiFeedbackSummaryMapper;

    public AiFeedbackSummaryQueryTools(AiFeedbackSummaryMapper aiFeedbackSummaryMapper) {
        this.aiFeedbackSummaryMapper = aiFeedbackSummaryMapper;
    }

    @Tool(name = "queryRecentFeedbackSummaryResults", description = "查询最近的AI面试反馈摘要结果，可指定查询数量")
    public String queryRecentFeedbackSummaryResults(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        try {
            int size = limit == null ? 5 : limit;
            size = Math.max(1, Math.min(size, 10));

            LambdaQueryWrapper<AiFeedbackSummary> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AiFeedbackSummary::getCreatedAt).last("limit " + size);

            List<AiFeedbackSummary> results = aiFeedbackSummaryMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "当前没有AI面试反馈摘要结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询AI面试反馈摘要失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryFeedbackSummaryByInterviewId", description = "根据面试ID查询AI面试反馈摘要结果")
    public String queryFeedbackSummaryByInterviewId(
            @ToolParam(description = "面试ID") Long interviewId
    ) {
        try {
            if (interviewId == null) {
                return "面试ID不能为空。";
            }

            LambdaQueryWrapper<AiFeedbackSummary> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiFeedbackSummary::getInterviewId, interviewId)
                    .orderByDesc(AiFeedbackSummary::getCreatedAt)
                    .last("limit 1");

            AiFeedbackSummary result = aiFeedbackSummaryMapper.selectOne(wrapper);

            if (result == null) {
                return "未查询到面试ID为 " + interviewId + " 的AI面试反馈摘要结果。";
            }

            return formatResult(result);
        } catch (Exception e) {
            return "根据面试ID查询AI面试反馈摘要失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryFeedbackSummaryByCandidateId", description = "根据候选人ID查询AI面试反馈摘要结果")
    public String queryFeedbackSummaryByCandidateId(
            @ToolParam(description = "候选人ID") Long candidateId
    ) {
        try {
            if (candidateId == null) {
                return "候选人ID不能为空。";
            }

            LambdaQueryWrapper<AiFeedbackSummary> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiFeedbackSummary::getCandidateId, candidateId)
                    .orderByDesc(AiFeedbackSummary::getCreatedAt)
                    .last("limit 5");

            List<AiFeedbackSummary> results = aiFeedbackSummaryMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "未查询到候选人ID为 " + candidateId + " 的AI面试反馈摘要结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "根据候选人ID查询AI面试反馈摘要失败：" + e.getMessage();
        }
    }

    private String formatResult(AiFeedbackSummary result) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String generatedAt = result.getGeneratedAt() == null
                ? "-"
                : result.getGeneratedAt().format(formatter);

        return "结果ID：" + result.getId()
                + "，任务ID：" + result.getTaskId()
                + "，面试ID：" + result.getInterviewId()
                + "，候选人ID：" + result.getCandidateId()
                + "，岗位ID：" + result.getJobId()
                + "，摘要：" + safe(result.getSummary())
                + "，优势：" + safe(result.getAdvantages())
                + "，风险点：" + safe(result.getRisks())
                + "，建议：" + safe(result.getSuggestion())
                + "，来源：" + safe(result.getSource())
                + "，生成时间：" + generatedAt;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
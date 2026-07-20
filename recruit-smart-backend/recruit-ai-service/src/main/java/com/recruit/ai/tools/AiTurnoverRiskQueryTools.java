package com.recruit.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.ai.entity.AiTurnoverRiskResult;
import com.recruit.ai.mapper.AiTurnoverRiskResultMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiTurnoverRiskQueryTools {

    private final AiTurnoverRiskResultMapper aiTurnoverRiskResultMapper;

    public AiTurnoverRiskQueryTools(AiTurnoverRiskResultMapper aiTurnoverRiskResultMapper) {
        this.aiTurnoverRiskResultMapper = aiTurnoverRiskResultMapper;
    }

    @Tool(name = "queryRecentTurnoverRiskResults", description = "查询最近的AI离职风险预测结果，可指定查询数量")
    public String queryRecentTurnoverRiskResults(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        try {
            int size = limit == null ? 5 : limit;
            size = Math.max(1, Math.min(size, 10));

            LambdaQueryWrapper<AiTurnoverRiskResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AiTurnoverRiskResult::getCreatedAt).last("limit " + size);

            List<AiTurnoverRiskResult> results = aiTurnoverRiskResultMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "当前没有AI离职风险预测结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询AI离职风险预测结果失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryTurnoverRiskByEmployeeId", description = "根据员工ID查询AI离职风险预测结果")
    public String queryTurnoverRiskByEmployeeId(
            @ToolParam(description = "员工ID") Long employeeId
    ) {
        try {
            if (employeeId == null) {
                return "员工ID不能为空。";
            }

            LambdaQueryWrapper<AiTurnoverRiskResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiTurnoverRiskResult::getEmployeeId, employeeId)
                    .orderByDesc(AiTurnoverRiskResult::getCreatedAt)
                    .last("limit 1");

            AiTurnoverRiskResult result = aiTurnoverRiskResultMapper.selectOne(wrapper);

            if (result == null) {
                return "未查询到员工ID为 " + employeeId + " 的AI离职风险预测结果。";
            }

            return formatResult(result);
        } catch (Exception e) {
            return "根据员工ID查询AI离职风险失败：" + e.getMessage();
        }
    }

    @Tool(name = "queryHighTurnoverRiskResults", description = "查询最近高离职风险员工的AI预测结果")
    public String queryHighTurnoverRiskResults(
            @ToolParam(description = "查询数量，默认5条，最大10条") Integer limit
    ) {
        try {
            int size = limit == null ? 5 : limit;
            size = Math.max(1, Math.min(size, 10));

            LambdaQueryWrapper<AiTurnoverRiskResult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiTurnoverRiskResult::getRiskLevel, "HIGH")
                    .orderByDesc(AiTurnoverRiskResult::getCreatedAt)
                    .last("limit " + size);

            List<AiTurnoverRiskResult> results = aiTurnoverRiskResultMapper.selectList(wrapper);

            if (results == null || results.isEmpty()) {
                return "当前没有高离职风险预测结果。";
            }

            return results.stream()
                    .map(this::formatResult)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "查询高离职风险结果失败：" + e.getMessage();
        }
    }

    private String formatResult(AiTurnoverRiskResult result) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String generatedAt = result.getGeneratedAt() == null
                ? "-"
                : result.getGeneratedAt().format(formatter);

        return "结果ID：" + result.getId()
                + "，任务ID：" + result.getTaskId()
                + "，员工ID：" + result.getEmployeeId()
                + "，风险等级：" + safe(result.getRiskLevel())
                + "，风险分数：" + result.getRiskScore()
                + "，摘要：" + safe(result.getSummary())
                + "，风险原因：" + safe(result.getRiskReasons())
                + "，干预建议：" + safe(result.getSuggestions())
                + "，来源：" + safe(result.getSource())
                + "，生成时间：" + generatedAt;
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
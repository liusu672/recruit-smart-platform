package com.recruit.biz.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.biz.vo.InterviewScoreItemVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InterviewScorecardCodec {

    private final ObjectMapper objectMapper;

    public String write(List<?> scorecard) {
        try {
            return objectMapper.writeValueAsString(scorecard);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "评分卡序列化失败"
            );
        }
    }

    public List<InterviewScoreItemVO> read(String scorecardJson) {
        if (scorecardJson == null || scorecardJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(scorecardJson);
            if (root.isArray()) {
                return objectMapper.convertValue(
                        root,
                        new TypeReference<List<InterviewScoreItemVO>>() {
                        }
                );
            }
            if (root.isObject()) {
                return readLegacyScorecard(root);
            }
            throw new IllegalArgumentException("评分卡必须是数组或对象");
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "评分卡数据格式不正确"
            );
        }
    }

    private List<InterviewScoreItemVO> readLegacyScorecard(JsonNode root) {
        List<LegacyScoreItem> legacyItems = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();
            if (!value.isIntegralNumber()) {
                throw new IllegalArgumentException("历史评分值不是整数");
            }
            int score = value.intValue();
            if (score < 1 || score > 5) {
                throw new IllegalArgumentException("历史评分值超出范围");
            }
            legacyItems.add(new LegacyScoreItem(field.getKey(), score));
        }

        boolean legacyFivePointScale = legacyItems.stream()
                .anyMatch(item -> item.score() > 4);
        return legacyItems.stream()
                .map(item -> new InterviewScoreItemVO(
                        legacyKey(item.label()),
                        item.label(),
                        "历史评分项，已转换为当前评分卡格式",
                        normalizeLegacyScore(item.score(), legacyFivePointScale),
                        "历史数据未记录评价证据"
                ))
                .toList();
    }

    private Integer normalizeLegacyScore(
            int score,
            boolean legacyFivePointScale
    ) {
        if (!legacyFivePointScale) {
            return score;
        }
        return Math.max(1, Math.round(score * 4.0f / 5.0f));
    }

    private String legacyKey(String label) {
        return switch (label) {
            case "专业能力" -> "professional";
            case "问题分析", "问题解决" -> "problem-solving";
            case "团队协作", "协作与影响力" -> "collaboration";
            case "沟通表达" -> "communication";
            case "岗位动机" -> "motivation";
            default -> "legacy-" + Integer.toHexString(label.hashCode());
        };
    }

    private record LegacyScoreItem(String label, int score) {
    }
}

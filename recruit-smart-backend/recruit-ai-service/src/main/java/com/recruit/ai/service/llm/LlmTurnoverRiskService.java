package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.prompt.TurnoverRiskPrompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmTurnoverRiskService {

    private final ChatClient.Builder chatClientBuilder;

    private final ObjectMapper objectMapper;

    public TurnoverRiskResponse predict(
            TurnoverRiskRequest request,
            String knowledgeContext
    ) {
        try {
            String content = chatClientBuilder.build()
                    .prompt()
                    .system(TurnoverRiskPrompts.SYSTEM_PROMPT)
                    .user(TurnoverRiskPrompts.buildUserPrompt(
                            request,
                            knowledgeContext
                    ))
                    .call()
                    .content();

            String json = extractJson(content);

            TurnoverRiskResponse response =
                    objectMapper.readValue(
                            json,
                            TurnoverRiskResponse.class
                    );

            validateResponse(response);

            return response;
        } catch (Exception e) {
            log.error("大模型离职风险分析失败", e);
            throw new IllegalStateException(
                    "大模型离职风险分析失败",
                    e
            );
        }
    }

    private void validateResponse(TurnoverRiskResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("大模型返回对象为空");
        }

        Set<String> sentimentLabels = Set.of(
                "POSITIVE",
                "NEUTRAL",
                "NEGATIVE"
        );

        Set<String> riskLevels = Set.of(
                "LOW",
                "MEDIUM",
                "HIGH"
        );

        if (!sentimentLabels.contains(
                response.getSentimentLabel()
        )) {
            throw new IllegalArgumentException(
                    "sentimentLabel不正确"
            );
        }

        if (!riskLevels.contains(
                response.getRiskLevel()
        )) {
            throw new IllegalArgumentException(
                    "riskLevel不正确"
            );
        }

        validateScore(
                response.getSentimentRiskScore(),
                "sentimentRiskScore"
        );

        validateScore(
                response.getRiskScore(),
                "riskScore"
        );

        if (response.getSentimentSummary() == null) {
            throw new IllegalArgumentException(
                    "sentimentSummary不能为空"
            );
        }

        if (response.getSummary() == null) {
            throw new IllegalArgumentException(
                    "summary不能为空"
            );
        }

        if (response.getRiskReasons() == null) {
            response.setRiskReasons(List.of());
        }

        if (response.getSuggestions() == null) {
            response.setSuggestions(List.of());
        }
    }

    private void validateScore(Integer score, String fieldName) {
        if (score == null || score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    fieldName + "必须在0到100之间"
            );
        }
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "大模型返回内容为空"
            );
        }

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start < 0 || end <= start) {
            throw new IllegalArgumentException(
                    "大模型返回内容不是合法JSON"
            );
        }

        return content.substring(start, end + 1);
    }
}
package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmTurnoverRiskServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ObjectMapper objectMapper;

    private LlmTurnoverRiskService service;

    @BeforeEach
    void setUp() {
        service = new LlmTurnoverRiskService(chatClientBuilder, objectMapper);
    }

    @Test
    void predict_withValidResponse_returnsResult() throws Exception {
        String json = "{\"sentimentLabel\":\"POSITIVE\",\"sentimentRiskScore\":10,\"sentimentSummary\":\"积极\",\"riskLevel\":\"LOW\",\"riskScore\":15,\"summary\":\"低风险\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse expected = createValidResponse();
        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(expected);

        TurnoverRiskRequest request = new TurnoverRiskRequest();
        TurnoverRiskResponse result = service.predict(request, "knowledge");

        assertEquals("POSITIVE", result.getSentimentLabel());
        assertEquals("LOW", result.getRiskLevel());
        assertEquals(10, result.getSentimentRiskScore());
        assertEquals(15, result.getRiskScore());
        assertNotNull(result.getRiskReasons());
        assertNotNull(result.getSuggestions());
    }

    @Test
    void predict_withNullRiskReasons_setsEmptyList() throws Exception {
        String json = "{\"sentimentLabel\":\"NEUTRAL\",\"sentimentRiskScore\":50,\"riskLevel\":\"MEDIUM\",\"riskScore\":50,\"summary\":\"中风险\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse parsed = createValidResponse();
        parsed.setSentimentLabel("NEUTRAL");
        parsed.setRiskLevel("MEDIUM");
        parsed.setSentimentRiskScore(50);
        parsed.setRiskScore(50);
        parsed.setSummary("中风险");
        parsed.setSentimentSummary("中性");
        parsed.setRiskReasons(null);
        parsed.setSuggestions(null);

        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(parsed);

        TurnoverRiskResponse result = service.predict(new TurnoverRiskRequest(), "ctx");

        assertNotNull(result.getRiskReasons());
        assertTrue(result.getRiskReasons().isEmpty());
        assertNotNull(result.getSuggestions());
        assertTrue(result.getSuggestions().isEmpty());
    }

    @Test
    void predict_withInvalidSentimentLabel_throwsException() throws Exception {
        String json = "{\"sentimentLabel\":\"INVALID\",\"sentimentRiskScore\":50,\"riskLevel\":\"MEDIUM\",\"riskScore\":50,\"sentimentSummary\":\"x\",\"summary\":\"中\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse bad = createValidResponse();
        bad.setSentimentLabel("INVALID");
        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(bad);

        assertThrows(IllegalStateException.class,
                () -> service.predict(new TurnoverRiskRequest(), "ctx"));
    }

    @Test
    void predict_withInvalidRiskLevel_throwsException() throws Exception {
        String json = "{\"sentimentLabel\":\"POSITIVE\",\"sentimentRiskScore\":10,\"riskLevel\":\"URGENT\",\"riskScore\":10,\"sentimentSummary\":\"x\",\"summary\":\"低\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse bad = createValidResponse();
        bad.setRiskLevel("URGENT");
        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(bad);

        assertThrows(IllegalStateException.class,
                () -> service.predict(new TurnoverRiskRequest(), "ctx"));
    }

    @Test
    void predict_withNullScore_throwsException() throws Exception {
        String json = "{\"sentimentLabel\":\"POSITIVE\",\"sentimentRiskScore\":null,\"riskLevel\":\"LOW\",\"riskScore\":15,\"sentimentSummary\":\"x\",\"summary\":\"低\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse bad = createValidResponse();
        bad.setSentimentRiskScore(null);
        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(bad);

        assertThrows(IllegalStateException.class,
                () -> service.predict(new TurnoverRiskRequest(), "ctx"));
    }

    @Test
    void predict_withScoreOutOfRange_throwsException() throws Exception {
        String json = "{\"sentimentLabel\":\"POSITIVE\",\"sentimentRiskScore\":150,\"riskLevel\":\"LOW\",\"riskScore\":-1,\"sentimentSummary\":\"x\",\"summary\":\"低\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse bad = createValidResponse();
        bad.setSentimentRiskScore(150);
        bad.setRiskScore(-1);
        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(bad);

        assertThrows(IllegalStateException.class,
                () -> service.predict(new TurnoverRiskRequest(), "ctx"));
    }

    @Test
    void predict_withNullSummary_throwsException() throws Exception {
        String json = "{\"sentimentLabel\":\"POSITIVE\",\"sentimentRiskScore\":10,\"riskLevel\":\"LOW\",\"riskScore\":15,\"sentimentSummary\":null,\"summary\":null}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        TurnoverRiskResponse bad = createValidResponse();
        bad.setSentimentSummary(null);
        bad.setSummary(null);
        when(objectMapper.readValue(json, TurnoverRiskResponse.class)).thenReturn(bad);

        assertThrows(IllegalStateException.class,
                () -> service.predict(new TurnoverRiskRequest(), "ctx"));
    }

    @Test
    void predict_whenContentNull_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> service.predict(new TurnoverRiskRequest(), "ctx"));
    }

    private static TurnoverRiskResponse createValidResponse() {
        TurnoverRiskResponse r = new TurnoverRiskResponse();
        r.setSentimentLabel("POSITIVE");
        r.setSentimentRiskScore(10);
        r.setSentimentSummary("情绪积极");
        r.setRiskLevel("LOW");
        r.setRiskScore(15);
        r.setSummary("低风险");
        r.setRiskReasons(List.of());
        r.setSuggestions(List.of("保持关注"));
        return r;
    }
}

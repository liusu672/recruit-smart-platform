package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmFeedbackSummaryServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ObjectMapper objectMapper;

    private LlmFeedbackSummaryService service;

    @BeforeEach
    void setUp() {
        service = new LlmFeedbackSummaryService(chatClientBuilder, objectMapper);
    }

    @Test
    void generate_returnsParsedResponse() throws Exception {
        String json = "{\"summary\":\"优秀候选人\",\"suggestion\":\"建议录用\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        FeedbackSummaryResponse expected = new FeedbackSummaryResponse();
        expected.setSummary("优秀候选人");
        when(objectMapper.readValue(json, FeedbackSummaryResponse.class)).thenReturn(expected);

        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        FeedbackSummaryResponse result = service.generate(request, "knowledge");

        assertEquals("优秀候选人", result.getSummary());
    }

    @Test
    void generate_whenContentNull_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> service.generate(new FeedbackSummaryRequest(), "ctx"));
    }

    @Test
    void generate_whenChatClientThrows_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenThrow(new RuntimeException("API失败"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.generate(new FeedbackSummaryRequest(), "ctx"));
        assertTrue(ex.getMessage().contains("摘要生成失败"));
    }
}

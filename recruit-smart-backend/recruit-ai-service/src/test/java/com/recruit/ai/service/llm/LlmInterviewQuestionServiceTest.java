package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
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
class LlmInterviewQuestionServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ObjectMapper objectMapper;

    private LlmInterviewQuestionService service;

    @BeforeEach
    void setUp() {
        service = new LlmInterviewQuestionService(chatClientBuilder, objectMapper);
    }

    @Test
    void generate_returnsParsedResponse() throws Exception {
        String json = "{\"category\":\"Java后端\",\"summary\":\"已生成面试题\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        InterviewQuestionResponse expected = new InterviewQuestionResponse();
        expected.setCategory("Java后端");
        when(objectMapper.readValue(json, InterviewQuestionResponse.class)).thenReturn(expected);

        InterviewQuestionRequest request = new InterviewQuestionRequest();
        InterviewQuestionResponse result = service.generate(request, "knowledge");

        assertEquals("Java后端", result.getCategory());
    }

    @Test
    void generate_whenContentNull_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> service.generate(new InterviewQuestionRequest(), "ctx"));
    }

    @Test
    void generate_whenChatClientThrows_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenThrow(new RuntimeException("API失败"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.generate(new InterviewQuestionRequest(), "ctx"));
        assertTrue(ex.getMessage().contains("面试题生成失败"));
    }
}

package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmResumeMatchServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ObjectMapper objectMapper;

    private LlmResumeMatchService service;

    @BeforeEach
    void setUp() {
        service = new LlmResumeMatchService(chatClientBuilder, objectMapper);
    }

    @Test
    void match_returnsParsedResponse() throws Exception {
        String jsonResponse = "{\"score\":85,\"level\":\"HIGH\"}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(jsonResponse);

        ResumeMatchResponse expected = new ResumeMatchResponse();
        expected.setScore(85);
        expected.setLevel("HIGH");
        when(objectMapper.readValue(jsonResponse, ResumeMatchResponse.class)).thenReturn(expected);

        ResumeMatchRequest request = new ResumeMatchRequest();
        ResumeMatchResponse result = service.match(request, "knowledge");

        assertEquals(85, result.getScore());
        assertEquals("HIGH", result.getLevel());
    }

    @Test
    void match_whenJsonEmbeddedInMarkdown_extractsJson() throws Exception {
        String markdownResponse = "```json\n{\"score\":70,\"level\":\"MEDIUM\"}\n```";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(markdownResponse);

        ResumeMatchResponse expected = new ResumeMatchResponse();
        expected.setScore(70);
        expected.setLevel("MEDIUM");
        when(objectMapper.readValue(anyString(), eq(ResumeMatchResponse.class))).thenReturn(expected);

        ResumeMatchRequest request = new ResumeMatchRequest();
        ResumeMatchResponse result = service.match(request, "");

        assertEquals(70, result.getScore());
    }

    @Test
    void match_whenContentNull_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(null);

        ResumeMatchRequest request = new ResumeMatchRequest();
        assertThrows(RuntimeException.class, () -> service.match(request, "ctx"));
    }

    @Test
    void match_whenContentNotJson_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("这不是 JSON");

        ResumeMatchRequest request = new ResumeMatchRequest();
        assertThrows(RuntimeException.class, () -> service.match(request, "ctx"));
    }

    @Test
    void match_whenChatClientThrows_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenThrow(new RuntimeException("API调用失败"));

        ResumeMatchRequest request = new ResumeMatchRequest();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.match(request, "ctx"));
        assertTrue(ex.getMessage().contains("简历匹配失败"));
    }
}

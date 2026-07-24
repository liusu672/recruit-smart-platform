package com.recruit.ai.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.response.ResumeParseResponse;
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
class LlmResumeParseServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ObjectMapper objectMapper;

    private LlmResumeParseService service;

    @BeforeEach
    void setUp() {
        service = new LlmResumeParseService(chatClientBuilder, objectMapper);
    }

    @Test
    void extract_returnsParsedResponse() throws Exception {
        String json = "{\"skills\":[\"Java\",\"Spring\"]}";
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(json);

        ResumeParseResponse expected = new ResumeParseResponse();
        expected.setSkills(java.util.List.of("Java", "Spring"));
        when(objectMapper.readValue(anyString(), eq(ResumeParseResponse.class))).thenReturn(expected);

        ResumeParseResponse result = service.extract("简历文本");

        assertEquals(java.util.List.of("Java", "Spring"), result.getSkills());
    }

    @Test
    void extract_whenContentNull_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> service.extract("简历文本"));
    }

    @Test
    void extract_whenChatClientThrows_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenThrow(new RuntimeException("API失败"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.extract("简历文本"));
        assertTrue(ex.getMessage().contains("解析失败"));
    }

    @Test
    void extract_whenContentNotJson_throwsException() {
        when(chatClientBuilder.build().prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("这不是JSON");

        assertThrows(IllegalStateException.class,
                () -> service.extract("简历文本"));
    }
}

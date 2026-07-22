package com.recruit.ai.service.impl;

import com.recruit.ai.dto.response.AiStreamEventResponse;
import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.prompt.ToolChatPrompts;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolChatServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Test
    void chatReturnsModelContent() {
        when(chatClient.prompt()
                .system(ToolChatPrompts.SYSTEM_PROMPT)
                .user("今天几号？")
                .call()
                .content()).thenReturn("2026-07-22");

        ToolChatServiceImpl service = new ToolChatServiceImpl(chatClient);

        assertEquals("2026-07-22", service.chat(request(" 今天几号？ ")));
    }

    @Test
    void chatFailsWhenModelReturnsBlankContent() {
        when(chatClient.prompt()
                .system(ToolChatPrompts.SYSTEM_PROMPT)
                .user("今天几号？")
                .call()
                .content()).thenReturn(" ");

        ToolChatServiceImpl service = new ToolChatServiceImpl(chatClient);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.chat(request("今天几号？"))
        );

        assertEquals("AI工具调用未返回有效内容", exception.getMessage());
    }

    @Test
    void chatFailsClearlyWhenToolCallingThrows() {
        when(chatClient.prompt()
                .system(ToolChatPrompts.SYSTEM_PROMPT)
                .user("今天几号？")
                .call()
                .content()).thenThrow(new IllegalStateException("tool failed"));

        ToolChatServiceImpl service = new ToolChatServiceImpl(chatClient);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.chat(request("今天几号？"))
        );

        assertEquals("AI工具调用失败，请稍后重试", exception.getMessage());
    }

    @Test
    void streamChatWrapsModelDeltasAsTypedEvents() {
        when(chatClient.prompt()
                .system(ToolChatPrompts.SYSTEM_PROMPT)
                .user("今天几号？")
                .stream()
                .content()).thenReturn(Flux.just("2026", "", "-07-22"));

        ToolChatServiceImpl service = new ToolChatServiceImpl(chatClient);

        List<AiStreamEventResponse> events = collect(service.streamChat(request(" 今天几号？ ")));

        assertEquals("meta", events.get(0).getType());
        assertEquals("delta", events.get(1).getType());
        assertEquals("2026", events.get(1).getContent());
        assertEquals("delta", events.get(2).getType());
        assertEquals("-07-22", events.get(2).getContent());
        assertEquals("done", events.get(3).getType());
    }

    @Test
    void streamChatReturnsErrorEventWhenModelStreamFails() {
        when(chatClient.prompt()
                .system(ToolChatPrompts.SYSTEM_PROMPT)
                .user("今天几号？")
                .stream()
                .content()).thenReturn(Flux.error(new IllegalStateException("tool failed")));

        ToolChatServiceImpl service = new ToolChatServiceImpl(chatClient);

        List<AiStreamEventResponse> events = collect(service.streamChat(request("今天几号？")));

        assertEquals("meta", events.get(0).getType());
        assertEquals("error", events.get(1).getType());
        assertEquals("500", events.get(1).getCode());
        assertEquals("AI流式对话失败，请稍后重试", events.get(1).getMessage());
    }

    private static ToolChatRequest request(String message) {
        ToolChatRequest request = new ToolChatRequest();
        request.setMessage(message);
        return request;
    }

    private static List<AiStreamEventResponse> collect(
            Flux<ServerSentEvent<AiStreamEventResponse>> events
    ) {
        return events
                .map(ServerSentEvent::data)
                .filter(Objects::nonNull)
                .collectList()
                .block(Duration.ofSeconds(1));
    }
}

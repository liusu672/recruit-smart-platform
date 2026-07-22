package com.recruit.ai.controller;

import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.service.ToolChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(
        name = "AI工具调用测试接口",
        description = "测试Spring AI Tool Calling和流式对话功能"
)
@RestController
@RequestMapping("/api/ai/tool-chat")
@RequiredArgsConstructor
public class ToolChatController {

    private final ToolChatService toolChatService;

    @Operation(
            summary = "工具调用普通对话",
            description = "一次性返回完整回答"
    )
    @PostMapping
    public String chat(
            @Valid @RequestBody ToolChatRequest request
    ) {
        return toolChatService.chat(request);
    }

    @Operation(
            summary = "工具调用流式对话",
            description = "通过SSE逐段返回回答"
    )
    @PostMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> streamChat(
            @Valid @RequestBody ToolChatRequest request
    ) {
        return toolChatService.streamChat(request);
    }
}
package com.recruit.ai.controller;

import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.service.ToolChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI工具调用测试接口", description = "测试Spring AI Tool Calling功能")
@RestController
@RequestMapping("/api/ai/tool-chat")
@RequiredArgsConstructor
public class ToolChatController {

    private final ToolChatService toolChatService;

    @Operation(summary = "工具调用对话", description = "让大模型在需要时调用Java工具方法")
    @PostMapping
    public String chat(@Valid @RequestBody ToolChatRequest request) {
        return toolChatService.chat(request);
    }
}

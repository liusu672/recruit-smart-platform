package com.recruit.ai.service;

import com.recruit.ai.dto.request.ToolChatRequest;
import reactor.core.publisher.Flux;

public interface ToolChatService {
    String chat(ToolChatRequest request);
    Flux<String> streamChat(ToolChatRequest request);
}
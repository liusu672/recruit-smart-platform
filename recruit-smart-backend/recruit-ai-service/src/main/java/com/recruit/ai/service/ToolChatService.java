package com.recruit.ai.service;

import com.recruit.ai.dto.request.ToolChatRequest;
import com.recruit.ai.dto.response.AiStreamEventResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ToolChatService {
    String chat(ToolChatRequest request);

    Flux<ServerSentEvent<AiStreamEventResponse>> streamChat(ToolChatRequest request);
}

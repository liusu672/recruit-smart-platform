package com.recruit.ai.service;

import com.recruit.ai.dto.request.ToolChatRequest;

public interface ToolChatService {
    String chat(ToolChatRequest request);
}
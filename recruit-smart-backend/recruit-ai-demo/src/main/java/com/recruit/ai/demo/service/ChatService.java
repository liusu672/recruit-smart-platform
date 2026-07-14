package com.recruit.ai.demo.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> streamChat(String message);

    String chat(String message);
}

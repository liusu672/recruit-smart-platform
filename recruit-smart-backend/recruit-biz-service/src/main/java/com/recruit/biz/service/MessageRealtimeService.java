package com.recruit.biz.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface MessageRealtimeService {
    SseEmitter subscribe(Long userId);

    void publishChanged();
}

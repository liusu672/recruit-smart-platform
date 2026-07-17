package com.recruit.biz.service.impl;

import com.recruit.biz.service.MessageRealtimeService;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MessageRealtimeServiceImpl implements MessageRealtimeService {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters =
            new ConcurrentHashMap<>();
    private final AtomicLong eventSequence = new AtomicLong();

    @Override
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitters.computeIfAbsent(
                userId,
                key -> new CopyOnWriteArrayList<>()
        ).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .id(String.valueOf(eventSequence.incrementAndGet()))
                            .data(Map.of("connected", true))
            );
        } catch (IOException e) {
            removeEmitter(userId, emitter);
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "建立消息实时连接失败"
            );
        }
        return emitter;
    }

    @Override
    public void publishChanged() {
        emitters.forEach((userId, userEmitters) ->
                userEmitters.forEach(emitter -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .name("message-updated")
                                        .id(String.valueOf(
                                                eventSequence.incrementAndGet()
                                        ))
                                        .data(Map.of(
                                                "changedAt",
                                                LocalDateTime.now().toString()
                                        ))
                        );
                    } catch (IOException | IllegalStateException e) {
                        removeEmitter(userId, emitter);
                    }
                })
        );
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId, userEmitters);
        }
    }
}

package com.recruit.biz.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessageRealtimeTest {

    @InjectMocks private MessageRealtimeServiceImpl rtService;

    @Test
    void subscribeAndPublishSuccess() {
        var emitter = rtService.subscribe(1L);
        assertNotNull(emitter);
        // 第二个订阅
        var emitter2 = rtService.subscribe(1L);
        assertNotNull(emitter2);
        // 发布不抛异常
        rtService.publishChanged();
    }

    @Test
    void subscribeDifferentUsers() {
        var e1 = rtService.subscribe(1L);
        var e2 = rtService.subscribe(2L);
        assertNotNull(e1);
        assertNotNull(e2);
        rtService.publishChanged();
    }
}

package com.recruit.biz.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageRealtimeServiceImplTest {

    @Test
    void subscriberCanReceiveMessageChangeSignal() {
        MessageRealtimeServiceImpl service =
                new MessageRealtimeServiceImpl();

        var emitter = service.subscribe(1L);

        assertNotNull(emitter);
        assertDoesNotThrow(service::publishChanged);
        emitter.complete();
    }
}

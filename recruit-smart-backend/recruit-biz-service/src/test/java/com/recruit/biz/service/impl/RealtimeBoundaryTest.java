package com.recruit.biz.service.impl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class RealtimeBoundaryTest {
    @InjectMocks MessageRealtimeServiceImpl s;
    @Test void subscribeMultipleAndPublish() {
        var e1 = s.subscribe(1L); assertNotNull(e1);
        var e2 = s.subscribe(2L); assertNotNull(e2);
        var e3 = s.subscribe(1L); assertNotNull(e3);
        s.publishChanged();
    }
    @Test void publishEmptyEmitters() { s.publishChanged(); }
    @Test void subscribeThenComplete() {
        var e = s.subscribe(10L);
        e.complete();
        s.publishChanged();
    }
}

package com.recruit.biz.service.impl;

import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.enums.ProcessEventSource;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationProcessEventServiceImplTest {

    @Mock
    private ApplicationProcessEventMapper applicationProcessEventMapper;
    @InjectMocks
    private ApplicationProcessEventServiceImpl processEventService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void recordBusinessEventUsesCurrentUserSnapshot() {
        UserContext.set(new CurrentUser(8L, "hr01", "HR"));

        processEventService.record(
                1L,
                ProcessEventType.SCREENING_PASSED,
                "SCREENING",
                "SCREEN_PASSED",
                "符合岗位要求",
                ProcessRelatedType.APPLICATION,
                1L
        );

        ArgumentCaptor<ApplicationProcessEvent> captor =
                ArgumentCaptor.forClass(ApplicationProcessEvent.class);
        verify(applicationProcessEventMapper).insert(captor.capture());
        ApplicationProcessEvent event = captor.getValue();
        assertEquals("SCREENING_PASSED", event.getEventType());
        assertEquals("筛选通过", event.getTitle());
        assertEquals(8L, event.getOperatorId());
        assertEquals("HR", event.getOperatorRole());
        assertEquals("BUSINESS", event.getSourceType());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void recordAiEventAllowsMissingCurrentUser() {
        processEventService.record(
                1L,
                ProcessEventType.AI_MATCH_COMPLETED,
                null,
                null,
                "AI匹配完成",
                ProcessEventSource.AI,
                ProcessRelatedType.AI_MATCH,
                2L
        );

        ArgumentCaptor<ApplicationProcessEvent> captor =
                ArgumentCaptor.forClass(ApplicationProcessEvent.class);
        verify(applicationProcessEventMapper).insert(captor.capture());
        ApplicationProcessEvent event = captor.getValue();
        assertEquals("AI", event.getSourceType());
        assertNull(event.getOperatorId());
        assertNull(event.getOperatorRole());
    }
}

package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.enums.ProcessEventSource;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.MessageService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppProcessEventFullTest {

    @BeforeAll
    static void init() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), ApplicationProcessEvent.class);
    }

    @Mock private ApplicationProcessEventMapper applicationProcessEventMapper;
    @Mock private MessageService messageService;
    @InjectMocks private ApplicationProcessEventServiceImpl eventService;

    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void recordWithUserContextSendsNotification() {
        when(applicationProcessEventMapper.insert(any())).thenReturn(1);

        eventService.record(1L, ProcessEventType.APPLICATION_SUBMITTED,
                "A", "B", "desc", ProcessEventSource.BUSINESS,
                ProcessRelatedType.APPLICATION, 1L);

        verify(applicationProcessEventMapper).insert(any());
        verify(messageService).sendSystemMessage(any(), any());
    }

    @Test
    void recordWithoutSourceWithContext() {
        when(applicationProcessEventMapper.insert(any())).thenReturn(1);
        eventService.record(1L, ProcessEventType.SCREENING_PASSED,
                "A", "B", "desc", ProcessRelatedType.INTERVIEW, 1L);
        verify(applicationProcessEventMapper).insert(any());
    }
}

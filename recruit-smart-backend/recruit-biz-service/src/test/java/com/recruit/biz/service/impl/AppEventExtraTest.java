package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.enums.*;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.MessageService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AppEventExtraTest {
    @BeforeAll static void init() { TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),ApplicationProcessEvent.class); }
    @Mock ApplicationProcessEventMapper m; @Mock MessageService ms;
    @InjectMocks ApplicationProcessEventServiceImpl s;
    @BeforeEach void s1() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void s2() { UserContext.clear(); }
    @Test void t1() { when(m.insert(any())).thenReturn(1); s.record(1L,ProcessEventType.INTERVIEW_ASSIGNED,"A","B","d",ProcessEventSource.BUSINESS,ProcessRelatedType.INTERVIEW,1L); verify(m).insert(any()); }
    @Test void t2() { when(m.insert(any())).thenReturn(1); s.record(1L,ProcessEventType.OFFER_SENT,"A","B","d",ProcessEventSource.BUSINESS,ProcessRelatedType.OFFER,1L); verify(m).insert(any()); }
    @Test void t3() { when(m.insert(any())).thenReturn(1); s.record(1L,ProcessEventType.MATERIAL_SUBMITTED,"A","B","d",ProcessEventSource.BUSINESS,ProcessRelatedType.ONBOARDING,1L); verify(m).insert(any()); }
    @Test void t4() { when(m.insert(any())).thenReturn(1); s.record(1L,ProcessEventType.ONBOARDING_COMPLETED,"A","B","d",ProcessEventSource.BUSINESS,ProcessRelatedType.ONBOARDING,1L); verify(m).insert(any()); }
    @Test void t5() { when(m.insert(any())).thenReturn(1); s.record(1L,ProcessEventType.OFFER_REVOKED,"A","B","d",ProcessEventSource.BUSINESS,ProcessRelatedType.OFFER,1L); verify(m).insert(any()); }
}

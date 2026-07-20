package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.common.result.PageResult;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T07OnbTest {
    @BeforeAll static void init() { for(var c:new Class<?>[]{Onboarding.class,Offer.class,JobApplication.class,JobPosition.class,Candidate.class}) TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c); }
    @Mock OnboardingMapper obm; @Mock CandidateMapper cm; @Mock OfferMapper om;
    @Mock JobApplicationMapper jam; @Mock JobPositionMapper jpm;
    @Mock EmployeeProfileMapper epm; @Mock ApplicationProcessEventService pes;
    @InjectMocks OnboardingServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void listMyOnboardingEmpty() {
        UserContext.set(new CurrentUser(2L,"candidate","CANDIDATE"));
        Candidate c = new Candidate(); c.setId(10L); c.setUserId(2L);
        when(cm.selectOne(any())).thenReturn(c);
        when(obm.selectList(any())).thenReturn(List.of());
        assertTrue(s.listMyOnboarding().isEmpty());
    }
}

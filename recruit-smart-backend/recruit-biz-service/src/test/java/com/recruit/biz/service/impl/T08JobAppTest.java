package com.recruit.biz.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class T08JobAppTest {
    @BeforeAll static void init() { for(var c:new Class<?>[]{JobApplication.class,JobPosition.class,Candidate.class,Resume.class,Interview.class}) TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c); }
    @Mock JobApplicationMapper jam; @Mock JobPositionMapper jpm; @Mock CandidateMapper cm;
    @Mock ResumeMapper rm; @Mock InterviewMapper im; @Mock ApplicationProcessEventService pes;
    @InjectMocks JobApplicationServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE")); }
    @AfterEach void clear() { UserContext.clear(); }
    @Test void createAppPositionClosed() {
        Candidate c = new Candidate(); c.setId(10L); c.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(c);
        JobPosition j = new JobPosition(); j.setId(20L); j.setStatus("CLOSED");
        when(jpm.selectById(20L)).thenReturn(j);
        var dto = new com.recruit.biz.dto.JobApplicationCreateDTO();
        dto.setJobId(20L); dto.setResumeId(30L);
        assertThrows(com.recruit.common.exception.BusinessException.class, () -> s.createApplication(dto));
    }
}

package com.recruit.biz.service.impl;

import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewQueryTest {
    @BeforeAll static void init() { for(var c:new Class<?>[]{Interview.class,JobApplication.class,JobPosition.class,Candidate.class,SysUser.class,InterviewFeedback.class,Resume.class}) TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c); }
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock JobApplicationMapper jam;
    @Mock CandidateMapper cm; @Mock JobPositionMapper jpm; @Mock ResumeMapper rm;
    @Mock SysUserMapper sum; @Mock SysRoleMapper srm; @Mock com.recruit.biz.service.ApplicationProcessEventService pes;
    @InjectMocks InterviewServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void getDetailSuccess() {
        Interview i = new Interview(); i.setId(1L); i.setApplicationId(10L); i.setInterviewerId(2L); i.setStatus("SCHEDULED");
        when(im.selectById(1L)).thenReturn(i);
        when(jam.selectById(10L)).thenReturn(new JobApplication());
        when(jpm.selectById(any())).thenReturn(new JobPosition());
        when(cm.selectById(any())).thenReturn(new Candidate());
        when(sum.selectById(2L)).thenReturn(new SysUser());
        var vo = s.getDetail(1L);
        assertNotNull(vo);
    }
    @Test void getDetailNotFound() { when(im.selectById(99L)).thenReturn(null); assertThrows(BusinessException.class, () -> s.getDetail(99L)); }
    @Test void completeInterview() { Interview i = new Interview(); i.setId(1L); i.setStatus("SCHEDULED"); when(im.selectById(1L)).thenReturn(i); when(im.update(any(),any())).thenReturn(1); s.completeInterview(1L); verify(im).update(any(),any()); }
}

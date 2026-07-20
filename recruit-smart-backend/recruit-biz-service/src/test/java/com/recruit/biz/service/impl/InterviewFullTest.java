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
class InterviewFullTest {
    @BeforeAll static void init() {
        for(var c:new Class<?>[]{Interview.class,JobApplication.class,JobPosition.class,Candidate.class,SysUser.class,InterviewFeedback.class,Resume.class})
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);
    }
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock JobApplicationMapper jam;
    @Mock CandidateMapper cm; @Mock JobPositionMapper jpm; @Mock ResumeMapper rm;
    @Mock SysUserMapper sum; @Mock SysRoleMapper srm;
    @Mock ApplicationProcessEventService pes;
    @InjectMocks InterviewServiceImpl s;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void getDetailFull() {
        Interview i = new Interview(); i.setId(1L); i.setApplicationId(10L); i.setInterviewerId(2L); i.setStatus("SCHEDULED");
        lenient().when(im.selectById(1L)).thenReturn(i);
        lenient().when(jam.selectById(10L)).thenReturn(new JobApplication());
        lenient().when(jpm.selectById(any())).thenReturn(new JobPosition());
        lenient().when(cm.selectById(any())).thenReturn(new Candidate());
        lenient().when(sum.selectById(2L)).thenReturn(new SysUser());
        lenient().when(ifm.selectOne(any())).thenReturn(null);
        lenient().when(rm.selectOne(any())).thenReturn(new Resume());
        assertNotNull(s.getDetail(1L));
    }
    @Test void cancelInterview() {
        Interview i = new Interview(); i.setId(1L); i.setApplicationId(10L); i.setStatus("SCHEDULED"); i.setInterviewerId(2L);
        lenient().when(im.selectById(1L)).thenReturn(i);
        lenient().when(jam.selectById(10L)).thenReturn(new JobApplication());
        lenient().when(im.update(any(),any())).thenReturn(1);
        s.cancelInterview(1L);
        verify(im, atLeastOnce()).update(any(),any());
    }
}

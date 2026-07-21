package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * InterviewServiceImpl 补充测试：验证详情字段、权限、异常路径
 */
@ExtendWith(MockitoExtension.class)
class InterviewFullTest {
    @BeforeAll static void init() {
        for(var c:new Class<?>[]{Interview.class,JobApplication.class,JobPosition.class,
                Candidate.class,SysUser.class,InterviewFeedback.class,Resume.class})
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);
    }
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock JobApplicationMapper jam;
    @Mock CandidateMapper cm; @Mock JobPositionMapper jpm; @Mock ResumeMapper rm;
    @Mock SysUserMapper sum; @Mock SysRoleMapper srm;
    @Mock ApplicationProcessEventService pes;
    @InjectMocks InterviewServiceImpl s;

    @Test void getDetailReturnsCorrectFields() {
        // HR 视角查看面试详情，验证关键字段
        UserContext.set(new CurrentUser(1L,"hr","HR"));

        Interview i = new Interview(); i.setId(1L); i.setApplicationId(10L);
        i.setInterviewerId(2L); i.setStatus("SCHEDULED"); i.setRound("FIRST");
        when(im.selectById(1L)).thenReturn(i);

        JobApplication app = new JobApplication(); app.setId(10L);
        app.setCandidateId(100L); app.setJobId(20L);
        when(jam.selectById(10L)).thenReturn(app);

        JobPosition job = new JobPosition(); job.setId(20L); job.setTitle("Java开发");
        when(jpm.selectById(20L)).thenReturn(job);

        Candidate candidate = new Candidate(); candidate.setId(100L); candidate.setName("张三");
        when(cm.selectById(100L)).thenReturn(candidate);

        SysUser interviewer = new SysUser(); interviewer.setRealName("面试官李四");
        when(sum.selectById(2L)).thenReturn(interviewer);

        InterviewDetailVO vo = s.getDetail(1L);
        assertNotNull(vo);
        assertEquals("SCHEDULED", vo.getStatus());
        assertEquals("Java开发", vo.getJobTitle());
        assertEquals("张三", vo.getCandidateName());
        assertEquals("面试官李四", vo.getInterviewerName());
    }

    @Test void getDetailNotFoundThrows() {
        when(im.selectById(99L)).thenReturn(null);
        UserContext.set(new CurrentUser(1L,"hr","HR"));
        assertThrows(BusinessException.class, () -> s.getDetail(99L));
    }

    @Test void cancelInterviewNotFoundThrows() {
        when(im.selectById(99L)).thenReturn(null);
        UserContext.set(new CurrentUser(1L,"hr","HR"));
        assertThrows(BusinessException.class, () -> s.cancelInterview(99L));
    }
}

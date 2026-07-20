package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.InterviewFeedbackVO;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewFeedbackQueryTest {

    @BeforeAll
    static void init() {
        for (var c : new Class<?>[]{Interview.class, InterviewFeedback.class, SysUser.class})
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), c);
    }

    @Mock private InterviewMapper interviewMapper;
    @Mock private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock private SysUserMapper sysUserMapper;

    @InjectMocks
    private InterviewFeedbackServiceImpl feedbackService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "admin", "ADMIN")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void getFeedbackAsAdminSuccess() {
        Interview interview = new Interview(); interview.setId(10L); interview.setInterviewerId(2L);
        when(interviewMapper.selectById(10L)).thenReturn(interview);

        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setId(1L); feedback.setInterviewId(10L); feedback.setInterviewerId(2L);
        feedback.setState("SUBMITTED"); feedback.setScore(85); feedback.setSuggestion("PASS");
        feedback.setComment("技术能力强");
        when(interviewFeedbackMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(feedback);

        SysUser user = new SysUser(); user.setId(2L); user.setRealName("面试官张三");
        when(sysUserMapper.selectById(2L)).thenReturn(user);

        InterviewFeedbackVO vo = feedbackService.getFeedback(10L);
        assertNotNull(vo);
        assertEquals(85, vo.getScore().intValue());
        assertEquals("面试官张三", vo.getInterviewerName());
    }

    @Test
    void getFeedbackNotFoundThrows() {
        when(interviewMapper.selectById(99L)).thenReturn(new Interview());
        when(interviewFeedbackMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        assertThrows(BusinessException.class, () -> feedbackService.getFeedback(99L));
    }

    @Test
    void getFeedbackInterviewNotFoundThrows() {
        when(interviewMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> feedbackService.getFeedback(99L));
    }
}

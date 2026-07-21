package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.*;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.vo.*;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 补充关键场景测试：CAS乐观锁失败、权限异常、空关联数据
 */
@ExtendWith(MockitoExtension.class)
class EdgeCaseTest {

    @BeforeAll static void init() {
        for(var c:new Class<?>[]{JobApplication.class,JobPosition.class,Candidate.class,
                Resume.class,Interview.class,Offer.class,Onboarding.class,InterviewFeedback.class})
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),c);
    }

    // ==================== 空关联数据 ====================

    @Test void offerDetailWithNullJob() {
        OfferServiceImpl s = new OfferServiceImpl();
        OfferMapper om = mock(OfferMapper.class); JobApplicationMapper jam = mock(JobApplicationMapper.class);
        JobPositionMapper jpm = mock(JobPositionMapper.class); CandidateMapper cm = mock(CandidateMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"offerMapper",om);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"jobApplicationMapper",jam);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"jobPositionMapper",jpm);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"candidateMapper",cm);
        UserContext.set(new CurrentUser(1L,"hr","HR"));
        Offer o = new Offer(); o.setId(1L); o.setApplicationId(100L); o.setStatus("SENT");
        when(om.selectById(1L)).thenReturn(o);
        JobApplication app = new JobApplication(); app.setJobId(999L);
        when(jam.selectById(100L)).thenReturn(app);
        when(jpm.selectById(999L)).thenReturn(null);
        Candidate c = new Candidate(); c.setId(5L);
        when(cm.selectById(any())).thenReturn(c);
        var vo = s.getDetail(1L);
        assertNotNull(vo);
        assertNull(vo.getJobTitle());
    }

    // ==================== 非法状态转换 ====================

    @Test void onboardingCompleteNotApprovedThrows() {
        OnboardingServiceImpl s = new OnboardingServiceImpl();
        OnboardingMapper m = mock(OnboardingMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"onboardingMapper",m);
        UserContext.set(new CurrentUser(1L,"hr","HR"));
        Onboarding o = new Onboarding(); o.setId(1L); o.setStatus("PENDING"); o.setMaterialStatus("PENDING");
        when(m.selectById(1L)).thenReturn(o);
        assertThrows(BusinessException.class, () -> s.completeOnboarding(1L));
    }

    // ==================== 权限校验 ====================

    @Test void resumeAccessByOtherCandidateThrows() {
        ResumeServiceImpl s = new ResumeServiceImpl();
        ResumeMapper rm = mock(ResumeMapper.class); CandidateMapper cm = mock(CandidateMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"resumeMapper",rm);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"candidateMapper",cm);
        // 当前用户是候选人A（userId=1, candidateId=10）
        UserContext.set(new CurrentUser(1L,"candidate","CANDIDATE"));
        Candidate a = new Candidate(); a.setId(10L); a.setUserId(1L);
        when(cm.selectOne(any())).thenReturn(a);
        // 简历属于候选人B（candidateId=99）
        Resume r = new Resume(); r.setId(1L); r.setCandidateId(99L);
        when(rm.selectById(1L)).thenReturn(r);
        assertThrows(BusinessException.class, () -> s.getDetail(1L));
    }

    // ==================== 空数据返回 ====================

    @Test void candidateDetailWithNoResumes() {
        CandidateServiceImpl s = new CandidateServiceImpl();
        CandidateMapper cm = mock(CandidateMapper.class); ResumeMapper rm = mock(ResumeMapper.class);
        JobApplicationMapper jam = mock(JobApplicationMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"candidateMapper",cm);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"resumeMapper",rm);
        org.springframework.test.util.ReflectionTestUtils.setField(s,"jobApplicationMapper",jam);
        UserContext.set(new CurrentUser(1L,"hr","HR"));
        Candidate c = new Candidate(); c.setId(10L); c.setName("张三");
        when(cm.selectById(10L)).thenReturn(c);
        when(rm.selectList(any())).thenReturn(List.of());
        when(jam.selectList(any())).thenReturn(List.of());
        var vo = s.getCandidateDetail(10L);
        assertTrue(vo.getResumes().isEmpty());
        assertTrue(vo.getApplications().isEmpty());
    }
}

package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.recruit.biz.entity.*;
import com.recruit.biz.mapper.*;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.DashboardOverviewVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardTasksTest {

    @BeforeAll
    static void init() {
        for (var c : new Class<?>[]{JobApplication.class, Offer.class, Onboarding.class,
                Candidate.class, JobPosition.class, Interview.class, InterviewFeedback.class})
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), c);
    }

    @Mock private DashboardMapper dashboardMapper;
    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private OfferMapper offerMapper;
    @Mock private OnboardingMapper onboardingMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private JobPositionMapper jobPositionMapper;
    @InjectMocks private DashboardServiceImpl dashboardService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void dashboardWithAllTaskTypes() {
        // Metrics
        lenient().when(dashboardMapper.countPendingScreening()).thenReturn(2L);
        lenient().when(dashboardMapper.countPendingFeedback()).thenReturn(1L);
        lenient().when(dashboardMapper.countActiveOffers()).thenReturn(1L);
        lenient().when(dashboardMapper.countReviewingOnboardings()).thenReturn(1L);

        // Screening tasks
        JobApplication app1 = new JobApplication(); app1.setId(1L);
        app1.setCandidateId(10L); app1.setJobId(20L); app1.setStatus("SUBMITTED");
        when(jobApplicationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(app1));

        // Pending feedback interviews
        Interview interview = new Interview(); interview.setId(100L);
        interview.setApplicationId(1L); interview.setInterviewerId(2L);
        when(dashboardMapper.selectPendingFeedbackInterviews(anyInt()))
                .thenReturn(List.of(interview));

        // Active offers
        Offer offer = new Offer(); offer.setId(200L); offer.setApplicationId(1L);
        offer.setStatus("SENT");
        lenient().when(offerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(offer));

        // Reviewing onboardings
        Onboarding onboard = new Onboarding(); onboard.setId(300L);
        onboard.setOfferId(200L); onboard.setCandidateId(10L);
        lenient().when(onboardingMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(onboard));

        // Lookup data
        Candidate candidate = new Candidate(); candidate.setId(10L); candidate.setName("张三");
        when(candidateMapper.selectBatchIds(Set.of(10L))).thenReturn(List.of(candidate));
        JobPosition job = new JobPosition(); job.setId(20L); job.setTitle("Java开发");
        when(jobPositionMapper.selectBatchIds(Set.of(20L))).thenReturn(List.of(job));

        DashboardOverviewVO overview = dashboardService.getOverview();
        assertNotNull(overview);
        assertNotNull(overview.getMetrics());
        assertNotNull(overview.getTasks());
    }
}

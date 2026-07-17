package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.DashboardMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.vo.DashboardOverviewVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DashboardMapper dashboardMapper;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private OfferMapper offerMapper;
    @Mock
    private OnboardingMapper onboardingMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void emptyDashboardReturnsZeroMetricsAndNoTasks() {
        when(dashboardMapper.countPendingScreening()).thenReturn(null);
        when(dashboardMapper.countPendingFeedback()).thenReturn(0L);
        when(dashboardMapper.countActiveOffers()).thenReturn(0L);
        when(dashboardMapper.countReviewingOnboardings()).thenReturn(0L);
        when(jobApplicationMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of());
        when(dashboardMapper.selectPendingFeedbackInterviews(10))
                .thenReturn(List.of());
        when(offerMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of());
        when(onboardingMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of());

        DashboardOverviewVO overview = dashboardService.getOverview();

        assertEquals(0L, overview.getMetrics().getPendingScreening());
        assertEquals(0L, overview.getMetrics().getPendingFeedback());
        assertEquals(0L, overview.getMetrics().getActiveOffers());
        assertEquals(0L, overview.getMetrics().getReviewingOnboardings());
        assertTrue(overview.getTasks().isEmpty());
    }

    @Test
    void overviewAggregatesContextAndSortsLatestTaskFirst() {
        JobApplication application = new JobApplication();
        application.setId(1L);
        application.setCandidateId(2L);
        application.setJobId(3L);
        application.setStatus("SUBMITTED");
        application.setAppliedAt(LocalDateTime.of(2026, 7, 17, 9, 0));

        Offer offer = new Offer();
        offer.setId(4L);
        offer.setApplicationId(1L);
        offer.setStatus("SENT");
        offer.setUpdatedAt(LocalDateTime.of(2026, 7, 17, 10, 0));

        Candidate candidate = new Candidate();
        candidate.setId(2L);
        candidate.setName("张三");

        JobPosition job = new JobPosition();
        job.setId(3L);
        job.setTitle("Java后端工程师");

        when(dashboardMapper.countPendingScreening()).thenReturn(1L);
        when(dashboardMapper.countPendingFeedback()).thenReturn(0L);
        when(dashboardMapper.countActiveOffers()).thenReturn(1L);
        when(dashboardMapper.countReviewingOnboardings()).thenReturn(0L);
        when(jobApplicationMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(application));
        when(dashboardMapper.selectPendingFeedbackInterviews(10))
                .thenReturn(List.of());
        when(offerMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(offer));
        when(onboardingMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of());
        when(candidateMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(candidate));
        when(jobPositionMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(job));

        DashboardOverviewVO overview = dashboardService.getOverview();

        assertEquals(2, overview.getTasks().size());
        assertEquals("OFFER", overview.getTasks().get(0).getType());
        assertEquals("SCREENING", overview.getTasks().get(1).getType());
        assertEquals("张三", overview.getTasks().get(0).getCandidateName());
        assertEquals(
                "Java后端工程师",
                overview.getTasks().get(0).getJobTitle()
        );
        assertTrue(overview.getTasks().get(0).getTitle()
                .contains("等待候选人回复"));
    }
}

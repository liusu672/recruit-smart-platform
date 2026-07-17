package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import com.recruit.biz.assembler.PipelineAssembler;
import com.recruit.biz.dto.PipelineApplicationQueryDTO;
import com.recruit.biz.entity.AiMatchResult;
import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Resume;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.AiMatchResultMapper;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.ResumeMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.vo.PipelineApplicationDetailVO;
import com.recruit.biz.vo.PipelineApplicationSummaryVO;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(
                        new MybatisConfiguration(),
                        ""
                ),
                InterviewFeedback.class
        );
    }

    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private AiMatchResultMapper aiMatchResultMapper;
    @Mock
    private ApplicationProcessEventMapper applicationProcessEventMapper;
    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock
    private OfferMapper offerMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Spy
    private PipelineAssembler pipelineAssembler = new PipelineAssembler();

    @InjectMocks
    private PipelineServiceImpl pipelineService;

    @Test
    @SuppressWarnings("unchecked")
    void listPipelineReturnsLightweightSummary() {
        LocalDateTime appliedAt = LocalDateTime.of(2026, 7, 1, 9, 0);
        JobApplication application = application(appliedAt);

        Page<JobApplication> applicationPage = new Page<>(1, 10);
        applicationPage.setTotal(1L);
        applicationPage.setRecords(List.of(application));
        when(jobApplicationMapper.selectPage(
                any(Page.class),
                any(LambdaQueryWrapper.class)
        )).thenReturn(applicationPage);

        Candidate candidate = candidate();
        when(candidateMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(candidate));
        JobPosition job = job();
        when(jobPositionMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(job));

        AiMatchResult aiMatch = aiMatch(appliedAt);
        when(aiMatchResultMapper.selectList(any()))
                .thenReturn(List.of(aiMatch));

        SysUser owner = user(8L, "Recruiter");
        when(sysUserMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(owner));
        when(interviewMapper.selectList(any())).thenReturn(List.of());
        when(offerMapper.selectList(any())).thenReturn(List.of());

        PageResult<PipelineApplicationSummaryVO> result =
                pipelineService.listPipeline(
                        new PipelineApplicationQueryDTO()
                );

        assertEquals(1L, result.getTotal());
        PipelineApplicationSummaryVO record = result.getRecords().get(0);
        assertEquals("Test Candidate", record.getCandidateName());
        assertEquals("Java Engineer", record.getJobTitle());
        assertEquals(new BigDecimal("88.50"), record.getMatchScore());
        assertEquals("Recruiter", record.getOwnerName());
        assertEquals("在线投递", record.getSourceText());
        assertEquals("PASS", record.getReviewDecision());
    }

    @Test
    void getPipelineDetailAggregatesWorkflowData() {
        LocalDateTime appliedAt = LocalDateTime.of(2026, 7, 1, 9, 0);
        JobApplication application = application(appliedAt);
        when(jobApplicationMapper.selectById(1L)).thenReturn(application);

        Candidate candidate = candidate();
        when(candidateMapper.selectById(2L)).thenReturn(candidate);
        JobPosition job = job();
        when(jobPositionMapper.selectById(3L)).thenReturn(job);

        Resume resume = new Resume();
        resume.setId(4L);
        resume.setResumeName("Backend Resume");
        resume.setFileType("PDF");
        when(resumeMapper.selectById(4L)).thenReturn(resume);

        AiMatchResult aiMatch = aiMatch(appliedAt);
        when(aiMatchResultMapper.selectOne(any())).thenReturn(aiMatch);

        Interview interview = new Interview();
        interview.setId(5L);
        interview.setApplicationId(1L);
        interview.setInterviewerId(6L);
        interview.setCreatedBy(8L);
        interview.setRound("FIRST");
        interview.setMethod("ONLINE");
        interview.setStatus("COMPLETED");
        interview.setInterviewTime(appliedAt.plusDays(2));
        interview.setCreatedAt(appliedAt.plusDays(1));
        when(interviewMapper.selectList(any()))
                .thenReturn(List.of(interview));

        Offer offer = new Offer();
        offer.setId(7L);
        offer.setApplicationId(1L);
        offer.setSalary(new BigDecimal("15000"));
        offer.setEntryDate(LocalDate.of(2026, 8, 1));
        offer.setStatus("ACCEPTED");
        offer.setCreatedBy(8L);
        offer.setCreatedAt(appliedAt.plusDays(2));
        offer.setSentAt(appliedAt.plusDays(3));
        offer.setAcceptedAt(appliedAt.plusDays(4));
        when(offerMapper.selectOne(any())).thenReturn(offer);

        ApplicationProcessEvent processEvent = new ApplicationProcessEvent();
        processEvent.setId(10L);
        processEvent.setApplicationId(1L);
        processEvent.setTitle("提交面试反馈");
        processEvent.setDescription("面试评分90，建议通过");
        processEvent.setOperatorId(6L);
        processEvent.setOperatorRole("INTERVIEWER");
        processEvent.setSourceType("BUSINESS");
        processEvent.setRelatedType("INTERVIEW");
        processEvent.setRelatedId(5L);
        processEvent.setOccurredAt(appliedAt.plusDays(2));
        when(applicationProcessEventMapper.selectList(any()))
                .thenReturn(List.of(processEvent));

        when(sysUserMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                user(6L, "Interviewer"),
                user(8L, "Recruiter")
        ));

        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setInterviewId(5L);
        feedback.setScore(90);
        feedback.setSuggestion("PASS");
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(feedback);

        PipelineApplicationDetailVO result =
                pipelineService.getPipelineDetail(1L);

        assertEquals("Backend Resume", result.getResumeName());
        assertNotNull(result.getAiMatch());
        assertEquals("Interviewer", result.getInterview().getInterviewerName());
        assertEquals(90, result.getInterview().getFeedbackScore());
        assertEquals("ACCEPTED", result.getOffer().getStatus());
        assertFalse(result.getTimeline().isEmpty());
        assertEquals("提交面试反馈", result.getTimeline().get(0).getTitle());
        assertNotNull(result.getTimeline().get(0).getDescription());
        assertNotNull(result.getTimeline().get(0).getActorName());
    }

    private JobApplication application(LocalDateTime appliedAt) {
        JobApplication application = new JobApplication();
        application.setId(1L);
        application.setCandidateId(2L);
        application.setJobId(3L);
        application.setResumeId(4L);
        application.setReviewedBy(8L);
        application.setStatus("OFFERED");
        application.setSource("ONLINE");
        application.setAllowAdjustment(1);
        application.setAppliedAt(appliedAt);
        application.setCreatedAt(appliedAt);
        application.setReviewedAt(appliedAt.plusDays(1));
        application.setUpdatedAt(appliedAt.plusDays(1));
        return application;
    }

    private Candidate candidate() {
        Candidate candidate = new Candidate();
        candidate.setId(2L);
        candidate.setName("Test Candidate");
        candidate.setEducation("Bachelor");
        candidate.setYearsOfExperience(3);
        return candidate;
    }

    private JobPosition job() {
        JobPosition job = new JobPosition();
        job.setId(3L);
        job.setTitle("Java Engineer");
        job.setDepartment("Technology");
        return job;
    }

    private AiMatchResult aiMatch(LocalDateTime appliedAt) {
        AiMatchResult aiMatch = new AiMatchResult();
        aiMatch.setId(9L);
        aiMatch.setApplicationId(1L);
        aiMatch.setMatchScore(new BigDecimal("88.50"));
        aiMatch.setRecommendLevel("HIGH");
        aiMatch.setRecommendReason("Strong match");
        aiMatch.setGeneratedAt(appliedAt.plusHours(1));
        return aiMatch;
    }

    private SysUser user(Long id, String realName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRealName(realName);
        return user;
    }
}

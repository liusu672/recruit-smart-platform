package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.OfferCreateDTO;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.enums.InterviewStatus;
import com.recruit.biz.enums.JobApplicationStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiRoundOfferServiceTest {

    @BeforeAll
    static void initializeTableInfo() {
        initializeTableInfo(Offer.class);
        initializeTableInfo(Interview.class);
        initializeTableInfo(InterviewFeedback.class);
    }

    private static void initializeTableInfo(Class<?> entityType) {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                entityType
        );
    }

    @Mock
    private OfferMapper offerMapper;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock
    private OnboardingMapper onboardingMapper;
    @Mock
    private ApplicationProcessEventService processEventService;
    @InjectMocks
    private OfferServiceImpl offerService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void cannotCreateOfferBeforeAllRequiredRoundsHaveFeedback() {
        stubTwoRoundJob();
        when(interviewMapper.selectList(any()))
                .thenReturn(List.of(interview(1L, "FIRST"), interview(2L, "SECOND")));
        when(interviewFeedbackMapper.selectList(any()))
                .thenReturn(List.of(feedback(1L)));

        assertThrows(
                BusinessException.class,
                () -> offerService.createOffer(createDTO())
        );
        verify(offerMapper, never()).insert(any(Offer.class));
    }

    @Test
    void canCreateOfferAfterAllRequiredRoundsHaveSubmittedFeedback() {
        stubTwoRoundJob();
        when(interviewMapper.selectList(any()))
                .thenReturn(List.of(interview(1L, "FIRST"), interview(2L, "SECOND")));
        when(interviewFeedbackMapper.selectList(any()))
                .thenReturn(List.of(feedback(1L), feedback(2L)));
        when(offerMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            Offer offer = invocation.getArgument(0);
            offer.setId(20L);
            return 1;
        }).when(offerMapper).insert(any(Offer.class));

        Long id = offerService.createOffer(createDTO());

        assertEquals(20L, id);
        verify(offerMapper).insert(any(Offer.class));
    }

    @Test
    void cannotSendDraftOfferWhenARequiredRoundIsStillIncomplete() {
        Offer offer = new Offer();
        offer.setId(20L);
        offer.setApplicationId(10L);
        offer.setStatus("DRAFT");
        when(offerMapper.selectById(20L)).thenReturn(offer);
        stubTwoRoundJob();
        when(interviewMapper.selectList(any()))
                .thenReturn(List.of(interview(1L, "FIRST")));
        when(interviewFeedbackMapper.selectList(any()))
                .thenReturn(List.of(feedback(1L)));

        assertThrows(
                BusinessException.class,
                () -> offerService.sendOffer(20L)
        );
        verify(offerMapper, never()).update(isNull(), any());
    }

    private void stubTwoRoundJob() {
        JobApplication application = new JobApplication();
        application.setId(10L);
        application.setJobId(30L);
        application.setStatus(JobApplicationStatus.INTERVIEWING.name());
        when(jobApplicationMapper.selectById(10L)).thenReturn(application);

        JobPosition job = new JobPosition();
        job.setId(30L);
        job.setRequiredInterviewRounds(2);
        when(jobPositionMapper.selectById(30L)).thenReturn(job);
    }

    private Interview interview(Long id, String round) {
        Interview interview = new Interview();
        interview.setId(id);
        interview.setApplicationId(10L);
        interview.setRound(round);
        interview.setStatus(InterviewStatus.COMPLETED.name());
        return interview;
    }

    private InterviewFeedback feedback(Long interviewId) {
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setInterviewId(interviewId);
        feedback.setState("SUBMITTED");
        return feedback;
    }

    private OfferCreateDTO createDTO() {
        OfferCreateDTO dto = new OfferCreateDTO();
        dto.setApplicationId(10L);
        dto.setSalary(new BigDecimal("15000"));
        dto.setEntryDate(LocalDate.of(2026, 8, 1));
        dto.setProbationMonths(3);
        dto.setWorkLocation("武汉");
        return dto;
    }
}

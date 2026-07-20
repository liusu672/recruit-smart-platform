package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.OfferCreateDTO;
import com.recruit.biz.dto.OfferHRQueryDTO;
import com.recruit.biz.dto.OfferUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Onboarding;
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
import com.recruit.common.result.PageResult;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        for (Class<?> clazz : List.of(Offer.class, JobApplication.class, JobPosition.class,
                Candidate.class, Interview.class, InterviewFeedback.class, Onboarding.class)) {
            TableInfoHelper.initTableInfo(
                    new MapperBuilderAssistant(new MybatisConfiguration(), ""), clazz);
        }
    }

    @Mock private OfferMapper offerMapper;
    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private JobPositionMapper jobPositionMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private InterviewMapper interviewMapper;
    @Mock private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock private OnboardingMapper onboardingMapper;
    @Mock private ApplicationProcessEventService processEventService;

    @InjectMocks
    private OfferServiceImpl offerService;

    @BeforeEach
    void setUp() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }

    @AfterEach
    void clear() { UserContext.clear(); }

    private InterviewFeedback submittedFeedback() {
        InterviewFeedback f = new InterviewFeedback();
        f.setInterviewId(200L);
        f.setState("SUBMITTED");
        return f;
    }

    // ==================== createOffer() ====================

    @Test
    void createOfferSuccess() {
        when(jobApplicationMapper.selectById(100L)).thenReturn(application());
        when(jobPositionMapper.selectById(10L)).thenReturn(jobPosition());
        when(interviewMapper.selectList(any())).thenReturn(List.of(interview()));
        when(interviewFeedbackMapper.selectList(any())).thenReturn(List.of(submittedFeedback()));
        when(offerMapper.selectCount(any())).thenReturn(0L);
        doAnswer(inv -> { ((Offer)inv.getArgument(0)).setId(999L); return 1; })
                .when(offerMapper).insert(any(Offer.class));

        assertEquals(999L, offerService.createOffer(offerCreateDTO()));
        verify(offerMapper).insert(any(Offer.class));
        verify(processEventService).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createOfferApplicationNotFoundThrows() {
        when(jobApplicationMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class,
                () -> offerService.createOffer(offerCreateDTO(99L)));
    }

    @Test
    void createOfferDuplicateThrows() {
        when(jobApplicationMapper.selectById(100L)).thenReturn(application());
        when(jobPositionMapper.selectById(10L)).thenReturn(jobPosition());
        when(interviewMapper.selectList(any())).thenReturn(List.of(interview()));
        when(interviewFeedbackMapper.selectList(any())).thenReturn(List.of(submittedFeedback()));
        when(offerMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class,
                () -> offerService.createOffer(offerCreateDTO()));
    }

    // ==================== updateOffer() ====================

    @Test
    void updateOfferSuccess() {
        when(offerMapper.selectById(300L)).thenReturn(offer("DRAFT"));
        when(offerMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        offerService.updateOffer(300L, offerUpdateDTO());
        verify(offerMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void updateOfferNotDraftThrows() {
        when(offerMapper.selectById(300L)).thenReturn(offer("SENT"));
        assertThrows(BusinessException.class,
                () -> offerService.updateOffer(300L, offerUpdateDTO()));
    }

    // ==================== sendOffer() ====================

    @Test
    void sendOfferSuccess() {
        when(offerMapper.selectById(300L)).thenReturn(offer("DRAFT"));
        when(jobApplicationMapper.selectById(100L)).thenReturn(application());
        when(jobPositionMapper.selectById(10L)).thenReturn(jobPosition());
        when(interviewMapper.selectList(any())).thenReturn(List.of(interview()));
        when(interviewFeedbackMapper.selectList(any())).thenReturn(List.of(submittedFeedback()));
        when(offerMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(jobApplicationMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        offerService.sendOffer(300L);
        verify(offerMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        verify(processEventService).record(any(), any(), any(), any(), any(), any(), any());
    }

    // ==================== acceptOffer() ====================

    @Test
    void acceptOfferSuccess() {
        UserContext.set(new CurrentUser(2L, "candidate", "CANDIDATE"));
        lenient().when(offerMapper.selectById(300L)).thenReturn(offer("SENT"));
        Candidate candidate = new Candidate(); candidate.setId(5L); candidate.setUserId(2L);
        lenient().when(candidateMapper.selectOne(any())).thenReturn(candidate);
        lenient().when(jobApplicationMapper.selectById(100L)).thenReturn(application("OFFERED"));
        lenient().when(offerMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        lenient().when(jobApplicationMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        lenient().when(onboardingMapper.insert(any(Onboarding.class))).thenReturn(1);

        offerService.acceptOffer(300L);
        verify(onboardingMapper).insert(any(Onboarding.class));
    }

    @Test
    void acceptOfferNotSentThrows() {
        when(offerMapper.selectById(300L)).thenReturn(offer("DRAFT"));
        assertThrows(BusinessException.class, () -> offerService.acceptOffer(300L));
    }

    // ==================== rejectOffer() ====================

    @Test
    void rejectOfferSuccess() {
        UserContext.set(new CurrentUser(2L, "candidate", "CANDIDATE"));
        Offer o = acceptOfferFlow();
        when(offerMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(jobApplicationMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        offerService.rejectOffer(300L);
        verify(offerMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    // ==================== revokeOffer() ====================

    @Test
    void revokeOfferSuccess() {
        when(offerMapper.selectById(300L)).thenReturn(offer("SENT"));
        when(jobApplicationMapper.selectById(100L)).thenReturn(application("OFFERED"));
        when(offerMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(jobApplicationMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        offerService.revokeOffer(300L);
        verify(offerMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    // ==================== getDetail() ====================

    @Test
    void getDetailSuccess() {
        when(offerMapper.selectById(300L)).thenReturn(offer("SENT"));
        when(jobApplicationMapper.selectById(100L)).thenReturn(application("OFFERED"));
        when(jobPositionMapper.selectById(10L)).thenReturn(jobPosition());
        Candidate c = new Candidate(); c.setId(5L); c.setName("张三");
        when(candidateMapper.selectById(5L)).thenReturn(c);

        var detail = offerService.getDetail(300L);
        assertNotNull(detail);
        assertEquals("SENT", detail.getStatus());
    }

    // ==================== listOffers() ====================

    @Test
    void listOffersReturnsPagedResults() {
        Page<Offer> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(offer("SENT")));
        when(offerMapper.selectPage(any(), any())).thenReturn(page);
        when(jobApplicationMapper.selectBatchIds(Set.of(100L))).thenReturn(List.of(application()));
        when(jobPositionMapper.selectBatchIds(Set.of(10L))).thenReturn(List.of(jobPosition()));
        Candidate c = new Candidate(); c.setId(5L); c.setName("张三");
        when(candidateMapper.selectBatchIds(Set.of(5L))).thenReturn(List.of(c));

        PageResult<?> result = offerService.listOffers(new OfferHRQueryDTO());
        assertEquals(1L, result.getTotal());
    }

    // ==================== Factory Methods ====================

    private Offer offer(String status) {
        Offer o = new Offer(); o.setId(300L); o.setApplicationId(100L);
        o.setStatus(status); o.setSalary(new BigDecimal("12000"));
        o.setEntryDate(LocalDate.now()); o.setWorkLocation("武汉");
        return o;
    }

    private JobApplication application() { return application("INTERVIEWING"); }
    private JobApplication application(String status) {
        JobApplication a = new JobApplication(); a.setId(100L); a.setCandidateId(5L);
        a.setJobId(10L); a.setStatus(status);
        return a;
    }

    private JobPosition jobPosition() {
        JobPosition j = new JobPosition(); j.setId(10L); j.setTitle("Java开发");
        j.setDepartment("研发部"); j.setRequiredInterviewRounds(1);
        return j;
    }

    private Interview interview() {
        Interview i = new Interview(); i.setId(200L); i.setApplicationId(100L);
        i.setRound("FIRST"); i.setStatus("COMPLETED");
        return i;
    }

    private OfferCreateDTO offerCreateDTO() { return offerCreateDTO(100L); }
    private OfferCreateDTO offerCreateDTO(Long applicationId) {
        OfferCreateDTO dto = new OfferCreateDTO(); dto.setApplicationId(applicationId);
        dto.setSalary(new BigDecimal("12000"));
        dto.setEntryDate(LocalDate.now().plusDays(30));
        dto.setProbationMonths(3); dto.setWorkLocation("武汉");
        return dto;
    }

    private OfferUpdateDTO offerUpdateDTO() {
        OfferUpdateDTO dto = new OfferUpdateDTO(); dto.setSalary(new BigDecimal("13000"));
        dto.setEntryDate(LocalDate.now().plusDays(30));
        dto.setProbationMonths(3); dto.setWorkLocation("武汉");
        return dto;
    }

    private Offer acceptOfferFlow() {
        Offer o = offer("SENT");
        when(offerMapper.selectById(300L)).thenReturn(o);
        Candidate candidate = new Candidate(); candidate.setId(5L); candidate.setUserId(2L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);
        when(jobApplicationMapper.selectById(100L)).thenReturn(application("OFFERED"));
        return o;
    }
}

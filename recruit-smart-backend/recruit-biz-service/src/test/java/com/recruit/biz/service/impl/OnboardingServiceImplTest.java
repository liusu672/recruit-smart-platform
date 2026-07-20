package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.OnboardingCancelDTO;
import com.recruit.biz.dto.OnboardingMaterialRejectDTO;
import com.recruit.biz.dto.OnboardingQueryDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.entity.Onboarding;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Onboarding.class
        );
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Offer.class
        );
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                JobApplication.class
        );
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Candidate.class
        );
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                EmployeeProfile.class
        );
    }

    @Mock private OnboardingMapper onboardingMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private OfferMapper offerMapper;
    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private JobPositionMapper jobPositionMapper;
    @Mock private EmployeeProfileMapper employeeProfileMapper;
    @Mock private ApplicationProcessEventService processEventService;

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(1L, "hr", "HR"));
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    // ==================== submitMaterials() ====================

    @Test
    void submitMaterialsSuccess() {
        UserContext.set(new CurrentUser(2L, "candidate", "CANDIDATE"));
        Candidate candidate = candidate(10L, 2L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);

        Onboarding onboarding = onboarding(1L, 10L, "PENDING", "PENDING");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);
        when(onboardingMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        Offer offer = offer(100L, 200L, "ACCEPTED");
        when(offerMapper.selectById(100L)).thenReturn(offer);
        JobApplication app = jobApplication(200L, 10L, 50L, "OFFERED");
        when(jobApplicationMapper.selectById(200L)).thenReturn(app);

        onboardingService.submitMaterials(1L);

        verify(onboardingMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        verify(processEventService).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void submitMaterialsNonCandidateThrowsException() {
        assertThrows(BusinessException.class,
                () -> onboardingService.submitMaterials(1L));
    }

    @Test
    void submitMaterialsInvalidStatusThrowsException() {
        UserContext.set(new CurrentUser(2L, "candidate", "CANDIDATE"));
        Candidate candidate = candidate(10L, 2L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);

        Onboarding onboarding = onboarding(1L, 10L, "CANCELED", "PENDING");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);

        assertThrows(BusinessException.class,
                () -> onboardingService.submitMaterials(1L));
    }

    // ==================== approveMaterials() ====================

    @Test
    void approveMaterialsSuccess() {
        Onboarding onboarding = onboarding(1L, 10L, "REVIEWING", "REVIEWING");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);
        when(onboardingMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        Offer offer = offer(100L, 200L, "ACCEPTED");
        when(offerMapper.selectById(100L)).thenReturn(offer);
        JobApplication app = jobApplication(200L, 10L, 50L, "OFFERED");
        when(jobApplicationMapper.selectById(200L)).thenReturn(app);

        onboardingService.approveMaterials(1L);
        verify(onboardingMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void approveMaterialsNotReviewingThrowsException() {
        Onboarding onboarding = onboarding(1L, 10L, "APPROVED", "APPROVED");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);

        assertThrows(BusinessException.class,
                () -> onboardingService.approveMaterials(1L));
    }

    // ==================== rejectMaterials() ====================

    @Test
    void rejectMaterialsSuccess() {
        Onboarding onboarding = onboarding(1L, 10L, "REVIEWING", "REVIEWING");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);
        when(onboardingMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        OnboardingMaterialRejectDTO dto = new OnboardingMaterialRejectDTO();
        dto.setReason("材料不清晰");

        Offer offer = offer(100L, 200L, "ACCEPTED");
        when(offerMapper.selectById(100L)).thenReturn(offer);
        JobApplication app = jobApplication(200L, 10L, 50L, "OFFERED");
        when(jobApplicationMapper.selectById(200L)).thenReturn(app);

        onboardingService.rejectMaterials(1L, dto);
        verify(onboardingMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        verify(processEventService).record(any(), any(), any(), any(), any(), any(), any());
    }

    // ==================== completeOnboarding() ====================

    @Test
    void completeOnboardingSuccess() {
        Onboarding onboarding = onboarding(1L, 10L, "APPROVED", "APPROVED");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);

        Offer offer = offer(100L, 200L, "ACCEPTED");
        when(offerMapper.selectById(100L)).thenReturn(offer);

        JobApplication app = jobApplication(200L, 10L, 50L, "OFFERED");
        when(jobApplicationMapper.selectById(200L)).thenReturn(app);

        Candidate candidate = candidate(10L, 99L);
        candidate.setName("张三");
        candidate.setPhone("13800138000");
        candidate.setEmail("test@test.com");
        when(candidateMapper.selectById(10L)).thenReturn(candidate);

        JobPosition job = new JobPosition();
        job.setId(50L);
        job.setTitle("Java开发");
        job.setDepartment("研发部");
        when(jobPositionMapper.selectById(50L)).thenReturn(job);

        when(employeeProfileMapper.selectCount(any())).thenReturn(0L);
        when(employeeProfileMapper.insert(any(EmployeeProfile.class))).thenReturn(1);
        when(onboardingMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(jobApplicationMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(candidateMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        onboardingService.completeOnboarding(1L);

        ArgumentCaptor<EmployeeProfile> empCaptor = ArgumentCaptor.forClass(EmployeeProfile.class);
        verify(employeeProfileMapper).insert(empCaptor.capture());
        EmployeeProfile emp = empCaptor.getValue();
        assertTrue(emp.getEmployeeNo().startsWith("EMP"));
        assertEquals("PROBATION", emp.getStatus());
        assertEquals("张三", emp.getName());
        assertEquals(10L, emp.getCandidateId().longValue());
        assertEquals("研发部", emp.getDepartment());
        assertEquals("Java开发", emp.getPosition());

        verify(processEventService).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void completeOnboardingNotApprovedThrowsException() {
        Onboarding onboarding = onboarding(1L, 10L, "PENDING", "PENDING");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);

        assertThrows(BusinessException.class,
                () -> onboardingService.completeOnboarding(1L));
    }

    // ==================== cancelOnboarding() ====================

    @Test
    void cancelOnboardingSuccess() {
        Onboarding onboarding = onboarding(1L, 10L, "APPROVED", "APPROVED");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);

        Offer offer = offer(100L, 200L, "ACCEPTED");
        when(offerMapper.selectById(100L)).thenReturn(offer);

        JobApplication app = jobApplication(200L, 10L, 50L, "OFFERED");
        when(jobApplicationMapper.selectById(200L)).thenReturn(app);

        when(onboardingMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(jobApplicationMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        OnboardingCancelDTO dto = new OnboardingCancelDTO();
        dto.setReason("候选人放弃入职");

        onboardingService.cancelOnboarding(1L, dto);

        verify(onboardingMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        verify(jobApplicationMapper).update(eq(null), any(LambdaUpdateWrapper.class));
        verify(processEventService).record(any(), any(), any(), any(), eq("候选人放弃入职"), any(), any());
    }

    @Test
    void cancelOnboardingAlreadyCanceledReturnsSilently() {
        Onboarding onboarding = onboarding(1L, 10L, "CANCELED", "PENDING");
        when(onboardingMapper.selectById(1L)).thenReturn(onboarding);

        onboardingService.cancelOnboarding(1L, new OnboardingCancelDTO());

        verify(onboardingMapper, never()).update(any(), any());
    }

    // ==================== listOnboarding() ====================

    @Test
    void listOnboardingReturnsPagedResults() {
        Onboarding onboarding = onboarding(1L, 10L, "PENDING", "PENDING");
        Page<Onboarding> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(onboarding));
        when(onboardingMapper.selectPage(any(), any())).thenReturn(page);

        Candidate candidate = candidate(10L, 99L);
        candidate.setName("张三");
        when(candidateMapper.selectBatchIds(Set.of(10L))).thenReturn(List.of(candidate));

        Offer offer = offer(100L, 200L, "ACCEPTED");
        when(offerMapper.selectBatchIds(Set.of(100L))).thenReturn(List.of(offer));

        JobApplication app = jobApplication(200L, 10L, 50L, "OFFERED");
        when(jobApplicationMapper.selectBatchIds(Set.of(200L))).thenReturn(List.of(app));

        JobPosition job = new JobPosition();
        job.setId(50L);
        job.setTitle("Java开发");
        job.setDepartment("研发部");
        when(jobPositionMapper.selectBatchIds(Set.of(50L))).thenReturn(List.of(job));

        PageResult<?> result = onboardingService.listOnboarding(new OnboardingQueryDTO());

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    // ==================== Factory Methods ====================

    private Onboarding onboarding(Long id, Long candidateId, String status, String materialStatus) {
        Onboarding o = new Onboarding();
        o.setId(id);
        o.setOfferId(100L);
        o.setCandidateId(candidateId);
        o.setStatus(status);
        o.setCurrentStep("步骤");
        o.setMaterialStatus(materialStatus);
        return o;
    }

    private Offer offer(Long id, Long applicationId, String status) {
        Offer o = new Offer();
        o.setId(id);
        o.setApplicationId(applicationId);
        o.setStatus(status);
        return o;
    }

    private JobApplication jobApplication(Long id, Long candidateId, Long jobId, String status) {
        JobApplication a = new JobApplication();
        a.setId(id);
        a.setCandidateId(candidateId);
        a.setJobId(jobId);
        a.setStatus(status);
        return a;
    }

    private Candidate candidate(Long id, Long userId) {
        Candidate c = new Candidate();
        c.setId(id);
        c.setUserId(userId);
        c.setName("测试候选人");
        c.setPhone("13800138000");
        return c;
    }
}

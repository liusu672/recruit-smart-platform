package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.EmployeeQueryDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.Offer;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.OfferMapper;
import com.recruit.biz.mapper.OnboardingMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrontendContractServiceTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                EmployeeProfile.class
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
    private OnboardingMapper onboardingMapper;
    @Mock
    private ApplicationProcessEventService processEventService;
    @Mock
    private EmployeeProfileMapper employeeProfileMapper;
    @InjectMocks
    private OfferServiceImpl offerService;
    @InjectMocks
    private EmployeeProfileServiceImpl employeeProfileService;

    @BeforeEach
    void setUpUserContext() {
        UserContext.set(new CurrentUser(2L, "hr01", "HR"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void offerDetailContainsCandidateContactInformation() {
        Offer offer = new Offer();
        offer.setId(1L);
        offer.setApplicationId(2L);
        offer.setStatus("DRAFT");
        when(offerMapper.selectById(1L)).thenReturn(offer);

        JobApplication application = new JobApplication();
        application.setId(2L);
        application.setJobId(3L);
        application.setCandidateId(4L);
        application.setStatus("INTERVIEWING");
        when(jobApplicationMapper.selectById(2L)).thenReturn(application);

        JobPosition job = new JobPosition();
        job.setId(3L);
        job.setTitle("Java工程师");
        when(jobPositionMapper.selectById(3L)).thenReturn(job);

        Candidate candidate = new Candidate();
        candidate.setId(4L);
        candidate.setName("测试候选人");
        candidate.setPhone("13800000000");
        candidate.setEmail("candidate@example.com");
        when(candidateMapper.selectById(4L)).thenReturn(candidate);

        var detail = offerService.getDetail(1L);

        assertEquals("13800000000", detail.getPhone());
        assertEquals("candidate@example.com", detail.getEmail());
    }

    @Test
    @SuppressWarnings("unchecked")
    void employeeListContainsFieldsRequiredByFrontend() {
        LocalDateTime assessedAt = LocalDateTime.of(2026, 7, 15, 18, 0);
        EmployeeProfile employee = new EmployeeProfile();
        employee.setId(1L);
        employee.setUserId(5L);
        employee.setCandidateId(6L);
        employee.setOnboardingId(7L);
        employee.setEmployeeNo("EMP001");
        employee.setName("测试员工");
        employee.setDepartment("研发部");
        employee.setPosition("Java工程师");
        employee.setEntryDate(LocalDate.of(2026, 7, 15));
        employee.setStatus("ACTIVE");
        employee.setPerformanceSummary("绩效正常");
        employee.setAttendanceSummary("考勤正常");
        employee.setSatisfactionFeedback("满意度良好");
        employee.setTurnoverRiskLevel("LOW");
        employee.setRiskAssessedAt(assessedAt);
        employee.setCreatedAt(assessedAt.minusDays(1));
        employee.setUpdatedAt(assessedAt);

        Page<EmployeeProfile> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(List.of(employee));
        when(employeeProfileMapper.selectPage(
                any(Page.class),
                any(LambdaQueryWrapper.class)
        )).thenReturn(page);

        var result = employeeProfileService.listEmployees(
                new EmployeeQueryDTO()
        );
        var summary = result.getRecords().get(0);

        assertEquals(5L, summary.getUserId());
        assertEquals(7L, summary.getOnboardingId());
        assertEquals("LOW", summary.getTurnoverRiskLevel());
        assertEquals(assessedAt, summary.getRiskAssessedAt());
        assertEquals("绩效正常", summary.getPerformanceSummary());
        assertEquals(assessedAt, summary.getUpdatedAt());
    }
}

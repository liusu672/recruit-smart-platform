package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.JobApplicationScreeningDTO;
import com.recruit.biz.dto.JobApplicationStatusUpdateDTO;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationScreeningServiceTest {

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(
                        new MybatisConfiguration(),
                        ""
                ),
                JobApplication.class
        );
    }

    @BeforeEach
    void setUpUserContext() {
        UserContext.set(new CurrentUser(8L, "hr01", "HR"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void reviewScreeningPassUpdatesScreeningApplication() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());
        when(jobApplicationMapper.update(isNull(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(1);

        JobApplicationScreeningDTO dto = screeningDTO("PASS", null, "通过");
        jobApplicationService.reviewScreening(1L, dto);

        ArgumentCaptor<LambdaUpdateWrapper<JobApplication>> captor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(jobApplicationMapper).update(isNull(), captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    void reviewScreeningPendingRequiresNote() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());

        JobApplicationScreeningDTO dto = screeningDTO(
                "PENDING",
                null,
                " "
        );

        assertThrows(
                BusinessException.class,
                () -> jobApplicationService.reviewScreening(1L, dto)
        );
        verify(jobApplicationMapper, never()).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void reviewScreeningPendingKeepsApplicationInScreening() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());
        when(jobApplicationMapper.update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);

        JobApplicationScreeningDTO dto = screeningDTO(
                "PENDING",
                null,
                "需要补充项目证明"
        );

        jobApplicationService.reviewScreening(1L, dto);

        verify(jobApplicationMapper).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void reviewScreeningRejectRequiresReasonCode() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());

        JobApplicationScreeningDTO dto = screeningDTO(
                "REJECT",
                null,
                "技能不匹配"
        );

        assertThrows(
                BusinessException.class,
                () -> jobApplicationService.reviewScreening(1L, dto)
        );
        verify(jobApplicationMapper, never()).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void reviewScreeningRejectSavesReason() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());
        when(jobApplicationMapper.update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);

        JobApplicationScreeningDTO dto = screeningDTO(
                "REJECT",
                "SKILL_NOT_MATCH",
                "核心技能与岗位不匹配"
        );

        jobApplicationService.reviewScreening(1L, dto);

        verify(jobApplicationMapper).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void reviewScreeningRejectsNonScreeningApplication() {
        JobApplication application = screeningApplication();
        application.setStatus("SUBMITTED");
        when(jobApplicationMapper.selectById(1L)).thenReturn(application);

        JobApplicationScreeningDTO dto = screeningDTO("PASS", null, null);

        assertThrows(
                BusinessException.class,
                () -> jobApplicationService.reviewScreening(1L, dto)
        );
        verify(jobApplicationMapper, never()).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void reviewScreeningDetectsConcurrentUpdate() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());
        when(jobApplicationMapper.update(isNull(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0);

        JobApplicationScreeningDTO dto = screeningDTO("PASS", null, null);

        assertThrows(
                BusinessException.class,
                () -> jobApplicationService.reviewScreening(1L, dto)
        );
    }

    @Test
    void updateStatusStartsScreeningFromSubmitted() {
        JobApplication application = screeningApplication();
        application.setStatus("SUBMITTED");
        when(jobApplicationMapper.selectById(1L)).thenReturn(application);
        when(jobApplicationMapper.update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);

        JobApplicationStatusUpdateDTO dto =
                new JobApplicationStatusUpdateDTO();
        dto.setStatus("SCREENING");

        jobApplicationService.updateStatus(1L, dto);

        verify(jobApplicationMapper).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void updateStatusCannotBypassUnifiedScreening() {
        when(jobApplicationMapper.selectById(1L))
                .thenReturn(screeningApplication());
        JobApplicationStatusUpdateDTO dto =
                new JobApplicationStatusUpdateDTO();
        dto.setStatus("SCREEN_PASSED");

        assertThrows(
                BusinessException.class,
                () -> jobApplicationService.updateStatus(1L, dto)
        );
        verify(jobApplicationMapper, never()).update(
                isNull(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private JobApplication screeningApplication() {
        JobApplication application = new JobApplication();
        application.setId(1L);
        application.setStatus("SCREENING");
        return application;
    }

    private JobApplicationScreeningDTO screeningDTO(
            String decision,
            String reasonCode,
            String note
    ) {
        JobApplicationScreeningDTO dto = new JobApplicationScreeningDTO();
        dto.setDecision(decision);
        dto.setRejectReasonCode(reasonCode);
        dto.setNote(note);
        return dto;
    }
}

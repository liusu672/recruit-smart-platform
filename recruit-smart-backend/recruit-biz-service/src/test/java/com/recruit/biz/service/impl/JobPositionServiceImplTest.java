package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.JobPositionCreateDTO;
import com.recruit.biz.dto.JobPositionUpdateDTO;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPositionServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                JobPosition.class
        );
    }

    @Mock
    private JobPositionMapper jobPositionMapper;

    @InjectMocks
    private JobPositionServiceImpl jobPositionService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void createJobDefaultsToOneInterviewRound() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        when(jobPositionMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            JobPosition job = invocation.getArgument(0);
            job.setId(1L);
            return 1;
        }).when(jobPositionMapper).insert(any(JobPosition.class));

        jobPositionService.createJob(createDTO());

        ArgumentCaptor<JobPosition> captor =
                ArgumentCaptor.forClass(JobPosition.class);
        verify(jobPositionMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getRequiredInterviewRounds());
    }

    @Test
    void updateJobPreservesInterviewRoundsWhenFieldIsOmitted() {
        JobPosition oldJob = new JobPosition();
        oldJob.setId(1L);
        oldJob.setRequiredInterviewRounds(2);
        when(jobPositionMapper.selectById(1L)).thenReturn(oldJob);
        when(jobPositionMapper.selectCount(any())).thenReturn(0L);

        jobPositionService.updateJob(1L, updateDTO());

        ArgumentCaptor<JobPosition> captor =
                ArgumentCaptor.forClass(JobPosition.class);
        verify(jobPositionMapper).updateById(captor.capture());
        assertEquals(2, captor.getValue().getRequiredInterviewRounds());
    }

    @Test
    void publishedJobCannotChangeRequiredInterviewRounds() {
        JobPosition oldJob = new JobPosition();
        oldJob.setId(1L);
        oldJob.setStatus("OPEN");
        oldJob.setRequiredInterviewRounds(1);
        when(jobPositionMapper.selectById(1L)).thenReturn(oldJob);
        when(jobPositionMapper.selectCount(any())).thenReturn(0L);
        JobPositionUpdateDTO dto = updateDTO();
        dto.setRequiredInterviewRounds(2);

        assertThrows(
                BusinessException.class,
                () -> jobPositionService.updateJob(1L, dto)
        );
        verify(jobPositionMapper, never()).updateById(any(JobPosition.class));
    }

    @Test
    void closeJobClosesOnlyOpenJobAndSetsClosedAt() {
        JobPosition job = new JobPosition();
        job.setId(1L);
        job.setStatus("OPEN");
        when(jobPositionMapper.selectById(1L)).thenReturn(job);

        jobPositionService.closeJob(1L);

        ArgumentCaptor<JobPosition> captor = ArgumentCaptor.forClass(JobPosition.class);
        verify(jobPositionMapper).updateById(captor.capture());
        JobPosition updatedJob = captor.getValue();
        assertEquals("CLOSED", updatedJob.getStatus());
        assertNotNull(updatedJob.getClosedAt());
    }

    @Test
    void closeJobRejectsDraftJob() {
        JobPosition job = new JobPosition();
        job.setId(1L);
        job.setStatus("DRAFT");
        when(jobPositionMapper.selectById(1L)).thenReturn(job);

        assertThrows(BusinessException.class, () -> jobPositionService.closeJob(1L));
    }

    private JobPositionCreateDTO createDTO() {
        JobPositionCreateDTO dto = new JobPositionCreateDTO();
        dto.setTitle("Java工程师");
        dto.setDepartment("研发部");
        dto.setSalaryMin(new BigDecimal("10000"));
        dto.setSalaryMax(new BigDecimal("15000"));
        dto.setHeadcount(1);
        return dto;
    }

    private JobPositionUpdateDTO updateDTO() {
        JobPositionUpdateDTO dto = new JobPositionUpdateDTO();
        dto.setTitle("Java工程师");
        dto.setDepartment("研发部");
        dto.setSalaryMin(new BigDecimal("10000"));
        dto.setSalaryMax(new BigDecimal("15000"));
        dto.setHeadcount(1);
        return dto;
    }
}

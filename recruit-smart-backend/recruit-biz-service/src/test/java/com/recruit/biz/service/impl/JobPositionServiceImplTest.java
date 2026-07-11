package com.recruit.biz.service.impl;

import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPositionServiceImplTest {

    @Mock
    private JobPositionMapper jobPositionMapper;

    @InjectMocks
    private JobPositionServiceImpl jobPositionService;

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
}

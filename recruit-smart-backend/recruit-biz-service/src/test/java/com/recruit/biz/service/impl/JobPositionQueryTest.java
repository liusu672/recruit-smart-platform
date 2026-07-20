package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.JobPositionQueryDTO;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.JobPositionVO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPositionQueryTest {

    @Mock private JobPositionMapper jobPositionMapper;

    @InjectMocks
    private JobPositionServiceImpl jobPositionService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    private JobPosition job(Long id, String title, String status) {
        JobPosition j = new JobPosition(); j.setId(id); j.setTitle(title);
        j.setDepartment("研发部"); j.setStatus(status);
        j.setSalaryMin(new BigDecimal("10000")); j.setSalaryMax(new BigDecimal("20000"));
        j.setHeadcount(2);
        return j;
    }

    @Test
    void jobPagesReturnsPage() {
        Page<JobPosition> page = new Page<>(1, 10, 2);
        page.setRecords(List.of(job(1L, "Java开发", "OPEN"), job(2L, "前端开发", "OPEN")));
        when(jobPositionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageResult<JobPositionVO> result = jobPositionService.jobPages(new JobPositionQueryDTO());
        assertEquals(2L, result.getTotal());
        assertEquals("Java开发", result.getRecords().get(0).getTitle());
    }

    @Test
    void jobPagesEmpty() {
        Page<JobPosition> empty = new Page<>(1, 10, 0);
        empty.setRecords(List.of());
        when(jobPositionMapper.selectPage(any(), any())).thenReturn(empty);
        PageResult<JobPositionVO> result = jobPositionService.jobPages(null);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void getByIdSuccess() {
        when(jobPositionMapper.selectById(1L)).thenReturn(job(1L, "Java开发", "OPEN"));
        var vo = jobPositionService.getById(1L);
        assertNotNull(vo);
        assertEquals("Java开发", vo.getTitle());
    }

    @Test
    void getByIdNotFoundThrows() {
        when(jobPositionMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> jobPositionService.getById(99L));
    }

    @Test
    void openJobPagesOnlyOpen() {
        Page<JobPosition> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(job(1L, "Java", "OPEN")));
        when(jobPositionMapper.selectPage(any(), any())).thenReturn(page);

        var result = jobPositionService.openJobPages(null);
        assertEquals(1L, result.getTotal());
    }

    @Test
    void getOpenByIdSuccess() {
        when(jobPositionMapper.selectById(1L)).thenReturn(job(1L, "Java", "OPEN"));
        var vo = jobPositionService.getOpenById(1L);
        assertNotNull(vo);
    }

    @Test
    void getOpenByIdClosedThrows() {
        when(jobPositionMapper.selectById(1L)).thenReturn(job(1L, "Java", "CLOSED"));
        assertThrows(BusinessException.class, () -> jobPositionService.getOpenById(1L));
    }

    @Test
    void publishJobSuccess() {
        JobPosition j = job(1L, "Java", "DRAFT");
        when(jobPositionMapper.selectById(1L)).thenReturn(j);
        when(jobPositionMapper.updateById(any())).thenReturn(1);
        jobPositionService.publishJob(1L);
        verify(jobPositionMapper).updateById(any());
    }

    @Test
    void pauseAndResumeJob() {
        JobPosition j = job(1L, "Java", "OPEN");
        when(jobPositionMapper.selectById(1L)).thenReturn(j);
        when(jobPositionMapper.updateById(any())).thenReturn(1);
        jobPositionService.pauseJob(1L);
        verify(jobPositionMapper, times(1)).updateById(any());

        when(jobPositionMapper.selectById(1L)).thenReturn(j);
        jobPositionService.resumeJob(1L);
        verify(jobPositionMapper, times(2)).updateById(any());
    }
}

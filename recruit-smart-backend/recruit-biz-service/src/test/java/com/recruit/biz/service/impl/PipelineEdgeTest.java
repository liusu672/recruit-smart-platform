package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.PipelineApplicationQueryDTO;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.mapper.*;
import com.recruit.biz.assembler.PipelineAssembler;
import com.recruit.biz.security.UserContext;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineEdgeTest {

    @Mock private JobApplicationMapper jobApplicationMapper;
    @Mock private CandidateMapper candidateMapper;
    @Mock private JobPositionMapper jobPositionMapper;
    @Mock private ResumeMapper resumeMapper;
    @Mock private AiMatchResultMapper aiMatchResultMapper;
    @Mock private ApplicationProcessEventMapper applicationProcessEventMapper;
    @Mock private InterviewMapper interviewMapper;
    @Mock private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock private OfferMapper offerMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private PipelineAssembler pipelineAssembler;
    @InjectMocks private PipelineServiceImpl pipelineService;

    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void listPipelineWithStatusFilterReturnsEmpty() {
        PipelineApplicationQueryDTO dto = new PipelineApplicationQueryDTO();
        dto.setStatus("SCREENING");
        Page<JobApplication> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(List.of());
        when(jobApplicationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(emptyPage);

        PageResult<?> result = pipelineService.listPipeline(dto);
        assertEquals(0L, result.getTotal());
    }

    @Test
    void getPipelineDetailNotFoundThrows() {
        when(jobApplicationMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> pipelineService.getPipelineDetail(99L));
    }
}

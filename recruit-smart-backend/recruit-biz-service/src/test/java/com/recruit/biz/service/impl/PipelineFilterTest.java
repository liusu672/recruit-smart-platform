package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.PipelineApplicationQueryDTO;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.mapper.*;
import com.recruit.biz.assembler.PipelineAssembler;
import com.recruit.common.result.PageResult;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineFilterTest {
    @BeforeAll static void init() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""), JobApplication.class);
    }
    @Mock JobApplicationMapper jam; @Mock CandidateMapper cm; @Mock JobPositionMapper jpm;
    @Mock ResumeMapper rm; @Mock AiMatchResultMapper aim; @Mock ApplicationProcessEventMapper aem;
    @Mock InterviewMapper im; @Mock InterviewFeedbackMapper ifm; @Mock OfferMapper om;
    @Mock SysUserMapper sum; @Mock PipelineAssembler pa;
    @InjectMocks PipelineServiceImpl s;
    @Test void listWithStatusAndJobId() {
        var dto = new PipelineApplicationQueryDTO(); dto.setStatus("SCREENING"); dto.setJobId(5L);
        Page<JobApplication> p = new Page<>(1,10,0); p.setRecords(List.of());
        when(jam.selectPage(any(),any())).thenReturn(p);
        assertEquals(0L, s.listPipeline(dto).getTotal());
    }
    @Test void listWithInterviewStageExpandsToInterviewStatuses() {
        var dto = new PipelineApplicationQueryDTO(); dto.setStage("INTERVIEW");
        Page<JobApplication> p = new Page<>(1,10,0); p.setRecords(List.of());
        when(jam.selectPage(any(),any())).thenReturn(p);

        s.listPipeline(dto);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<JobApplication>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(jam).selectPage(any(), captor.capture());
        LambdaQueryWrapper<JobApplication> wrapper = captor.getValue();
        assertTrue(wrapper.getSqlSegment().contains("status IN"));
        Map<String, Object> params = wrapper.getParamNameValuePairs();
        assertTrue(params.containsValue("SCREEN_PASSED"));
        assertTrue(params.containsValue("INTERVIEWING"));
    }
    @Test void stageCountsReturnsAllSixEntries() {
        when(jam.selectList(any())).thenReturn(List.of(
                application("SUBMITTED"),
                application("SCREEN_PASSED"),
                application("INTERVIEWING"),
                application("SCREEN_REJECT"),
                application("REJECTED"),
                application("WITHDRAWN")
        ));

        var counts = s.listStageCounts(new PipelineApplicationQueryDTO());

        assertEquals(6, counts.size());
        assertEquals(1L, counts.get(0).getCount());
        assertEquals("NEW", counts.get(0).getStage());
        assertEquals(2L, counts.get(2).getCount());
        assertEquals("INTERVIEW", counts.get(2).getStage());
        assertEquals(3L, counts.get(5).getCount());
        assertEquals("CLOSED", counts.get(5).getStage());
    }
    @Test void listWithKeywordNoMatch() {
        var dto = new PipelineApplicationQueryDTO(); dto.setKeyword("nonexistent");
        PageResult<?> r = s.listPipeline(dto); assertEquals(0L, r.getTotal());
    }
    private JobApplication application(String status) {
        JobApplication application = new JobApplication();
        application.setStatus(status);
        return application;
    }
}

package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
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
    @Test void listWithKeywordNoMatch() {
        var dto = new PipelineApplicationQueryDTO(); dto.setKeyword("nonexistent");
        PageResult<?> r = s.listPipeline(dto); assertEquals(0L, r.getTotal());
    }
}

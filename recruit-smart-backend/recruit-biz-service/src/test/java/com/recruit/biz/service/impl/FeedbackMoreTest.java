package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.InterviewFeedbackDraftDTO;
import com.recruit.biz.dto.InterviewScoreItemDTO;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.support.InterviewScorecardCodec;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackMoreTest {
    @BeforeAll static void init() { TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""), InterviewFeedback.class); }
    @Mock InterviewMapper interviewMapper;
    @Mock InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock com.recruit.biz.mapper.SysUserMapper sysUserMapper;
    @Mock InterviewScorecardCodec scorecardCodec;
    @Mock ApplicationProcessEventService processEventService;
    @InjectMocks InterviewFeedbackServiceImpl fbService;
    @BeforeEach void setup() { UserContext.set(new CurrentUser(1L,"hr","HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test void saveDraftOnScheduled() {
        Interview i = new Interview(); i.setId(10L); i.setInterviewerId(1L); i.setStatus("SCHEDULED");
        when(interviewMapper.selectById(10L)).thenReturn(i);
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(null);
        when(interviewFeedbackMapper.insert(any())).thenReturn(1);
        InterviewFeedbackDraftDTO dto = new InterviewFeedbackDraftDTO();
        dto.setComment("good");
        fbService.saveDraft(10L, dto);
        verify(interviewFeedbackMapper).insert(any());
    }
}

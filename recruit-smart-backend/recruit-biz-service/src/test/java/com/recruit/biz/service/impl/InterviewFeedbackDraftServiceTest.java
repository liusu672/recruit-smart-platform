package com.recruit.biz.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.biz.dto.InterviewFeedbackCreateDTO;
import com.recruit.biz.dto.InterviewFeedbackDraftDTO;
import com.recruit.biz.dto.InterviewScoreItemDTO;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.InterviewFeedback;
import com.recruit.biz.mapper.InterviewFeedbackMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.support.InterviewScorecardCodec;
import com.recruit.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewFeedbackDraftServiceTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(
                        new MybatisConfiguration(),
                        ""
                ),
                InterviewFeedback.class
        );
    }

    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private InterviewFeedbackMapper interviewFeedbackMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private ApplicationProcessEventService processEventService;
    @Spy
    private InterviewScorecardCodec scorecardCodec =
            new InterviewScorecardCodec(new ObjectMapper());
    @InjectMocks
    private InterviewFeedbackServiceImpl interviewFeedbackService;

    @BeforeEach
    void setUp() {
        UserContext.set(new CurrentUser(6L, "interviewer", "INTERVIEWER"));
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void saveDraftCreatesDraftFeedback() {
        when(interviewMapper.selectById(1L)).thenReturn(interview("SCHEDULED"));
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(null);

        interviewFeedbackService.saveDraft(1L, draftDTO());

        ArgumentCaptor<InterviewFeedback> captor =
                ArgumentCaptor.forClass(InterviewFeedback.class);
        verify(interviewFeedbackMapper).insert(captor.capture());
        InterviewFeedback draft = captor.getValue();
        assertEquals("DRAFT", draft.getState());
        assertEquals(6L, draft.getInterviewerId());
        assertEquals(1, draft.getScore());
        assertEquals("待继续核实", draft.getComment());
    }

    @Test
    void saveDraftRejectsSubmittedFeedback() {
        when(interviewMapper.selectById(1L)).thenReturn(interview("COMPLETED"));
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setId(2L);
        feedback.setState("SUBMITTED");
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(feedback);

        assertThrows(
                BusinessException.class,
                () -> interviewFeedbackService.saveDraft(1L, draftDTO())
        );
        verify(interviewFeedbackMapper, never()).update(isNull(), any());
    }

    @Test
    void submitFeedbackPromotesExistingDraft() {
        when(interviewMapper.selectById(1L)).thenReturn(interview("COMPLETED"));
        InterviewFeedback draft = new InterviewFeedback();
        draft.setId(2L);
        draft.setInterviewId(1L);
        draft.setState("DRAFT");
        when(interviewFeedbackMapper.selectOne(any())).thenReturn(draft);
        when(interviewFeedbackMapper.update(isNull(), any())).thenReturn(1);

        Long feedbackId = interviewFeedbackService.submitFeedback(
                1L,
                createDTO()
        );

        assertEquals(2L, feedbackId);
        verify(interviewFeedbackMapper).update(isNull(), any());
        verify(processEventService).record(
                any(), any(), any(), any(), any(), any(), any()
        );
    }

    private Interview interview(String status) {
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setApplicationId(3L);
        interview.setInterviewerId(6L);
        interview.setStatus(status);
        return interview;
    }

    private InterviewFeedbackDraftDTO draftDTO() {
        InterviewFeedbackDraftDTO dto = new InterviewFeedbackDraftDTO();
        InterviewScoreItemDTO item = scoreItem(1, "待继续核实");
        dto.setScorecard(List.of(item));
        dto.setScore(1);
        dto.setComment("待继续核实");
        return dto;
    }

    private InterviewFeedbackCreateDTO createDTO() {
        InterviewFeedbackCreateDTO dto = new InterviewFeedbackCreateDTO();
        dto.setScorecard(List.of(scoreItem(4, "项目事实完整")));
        dto.setScore(100);
        dto.setComment("综合表现良好");
        dto.setSuggestion("PASS");
        return dto;
    }

    private InterviewScoreItemDTO scoreItem(
            Integer score,
            String evidence
    ) {
        InterviewScoreItemDTO item = new InterviewScoreItemDTO();
        item.setKey("professional");
        item.setLabel("专业能力");
        item.setDescription("核心能力");
        item.setScore(score);
        item.setEvidence(evidence);
        return item;
    }
}

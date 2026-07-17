package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.MessageConversationCreateDTO;
import com.recruit.biz.dto.MessageSendDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.MessageConversation;
import com.recruit.biz.entity.MessageConversationMember;
import com.recruit.biz.entity.MessageRecord;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.InterviewMapper;
import com.recruit.biz.mapper.JobApplicationMapper;
import com.recruit.biz.mapper.JobPositionMapper;
import com.recruit.biz.mapper.MessageConversationMapper;
import com.recruit.biz.mapper.MessageConversationMemberMapper;
import com.recruit.biz.mapper.MessageRecordMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.MessageRealtimeService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        initializeTableInfo(Candidate.class);
        initializeTableInfo(Interview.class);
        initializeTableInfo(MessageConversation.class);
        initializeTableInfo(MessageConversationMember.class);
        initializeTableInfo(MessageRecord.class);
    }

    private static void initializeTableInfo(Class<?> entityType) {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                entityType
        );
    }

    @Mock
    private MessageConversationMapper conversationMapper;
    @Mock
    private MessageConversationMemberMapper memberMapper;
    @Mock
    private MessageRecordMapper messageRecordMapper;
    @Mock
    private JobApplicationMapper jobApplicationMapper;
    @Mock
    private JobPositionMapper jobPositionMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private InterviewMapper interviewMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private MessageRealtimeService messageRealtimeService;
    @InjectMocks
    private MessageServiceImpl messageService;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void hrCanCreateConversationForApplication() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        when(jobApplicationMapper.selectById(10L))
                .thenReturn(application(10L, 20L));
        when(conversationMapper.selectOne(any())).thenReturn(null);
        when(memberMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            MessageConversation conversation = invocation.getArgument(0);
            conversation.setId(30L);
            return 1;
        }).when(conversationMapper).insert(any(MessageConversation.class));

        Long id = messageService.getOrCreateConversation(createDTO(10L));

        assertEquals(30L, id);
        verify(memberMapper).insert(any(MessageConversationMember.class));
    }

    @Test
    void assignedInterviewerCanSendMessage() {
        UserContext.set(new CurrentUser(3L, "interviewer", "INTERVIEWER"));
        MessageConversation conversation = new MessageConversation();
        conversation.setId(30L);
        conversation.setApplicationId(10L);
        when(conversationMapper.selectById(30L)).thenReturn(conversation);
        when(jobApplicationMapper.selectById(10L))
                .thenReturn(application(10L, 20L));
        when(interviewMapper.selectCount(any())).thenReturn(1L);
        when(memberMapper.selectCount(any())).thenReturn(0L);
        when(conversationMapper.update(isNull(), any())).thenReturn(1);
        when(memberMapper.update(isNull(), any())).thenReturn(1);
        doAnswer(invocation -> {
            MessageRecord message = invocation.getArgument(0);
            message.setId(40L);
            return 1;
        }).when(messageRecordMapper).insert(any(MessageRecord.class));
        MessageSendDTO dto = new MessageSendDTO();
        dto.setContent("  请确认面试时间  ");

        Long id = messageService.sendMessage(30L, dto);

        assertEquals(40L, id);
        ArgumentCaptor<MessageRecord> captor =
                ArgumentCaptor.forClass(MessageRecord.class);
        verify(messageRecordMapper).insert(captor.capture());
        assertEquals("请确认面试时间", captor.getValue().getContent());
        assertEquals(3L, captor.getValue().getSenderId());
        assertEquals("INTERVIEWER", captor.getValue().getSenderRole());
        assertEquals("TEXT", captor.getValue().getMessageType());
    }

    @Test
    void systemMessageHasSystemTypeAndNoSenderAccount() {
        MessageConversation conversation = new MessageConversation();
        conversation.setId(30L);
        conversation.setApplicationId(10L);
        when(jobApplicationMapper.selectById(10L))
                .thenReturn(application(10L, 20L));
        when(conversationMapper.selectOne(any())).thenReturn(conversation);
        when(conversationMapper.update(isNull(), any())).thenReturn(1);
        doAnswer(invocation -> {
            MessageRecord message = invocation.getArgument(0);
            message.setId(41L);
            return 1;
        }).when(messageRecordMapper).insert(any(MessageRecord.class));

        Long id = messageService.sendSystemMessage(
                10L,
                "面试时间已经确认。"
        );

        assertEquals(41L, id);
        ArgumentCaptor<MessageRecord> captor =
                ArgumentCaptor.forClass(MessageRecord.class);
        verify(messageRecordMapper).insert(captor.capture());
        assertEquals("SYSTEM", captor.getValue().getMessageType());
        assertEquals("SYSTEM", captor.getValue().getSenderRole());
        assertNull(captor.getValue().getSenderId());
        verify(messageRealtimeService).publishChanged();
    }

    @Test
    void hrCanCountUnreadMessagesAcrossVisibleConversations() {
        UserContext.set(new CurrentUser(2L, "hr", "HR"));
        MessageConversation conversation = new MessageConversation();
        conversation.setId(30L);
        conversation.setApplicationId(10L);
        when(conversationMapper.selectList(any()))
                .thenReturn(List.of(conversation));
        when(memberMapper.selectList(any())).thenReturn(List.of());
        when(messageRecordMapper.selectCount(any())).thenReturn(3L);

        Long unreadCount = messageService.countUnreadMessages();

        assertEquals(3L, unreadCount);
    }

    @Test
    void candidateCannotAccessAnotherCandidatesApplication() {
        UserContext.set(new CurrentUser(4L, "candidate", "CANDIDATE"));
        when(jobApplicationMapper.selectById(10L))
                .thenReturn(application(10L, 20L));
        Candidate candidate = new Candidate();
        candidate.setId(21L);
        candidate.setUserId(4L);
        when(candidateMapper.selectOne(any())).thenReturn(candidate);

        assertThrows(
                BusinessException.class,
                () -> messageService.getOrCreateConversation(createDTO(10L))
        );
        verify(conversationMapper, never()).insert(
                any(MessageConversation.class)
        );
    }

    private JobApplication application(Long id, Long candidateId) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setCandidateId(candidateId);
        application.setJobId(1L);
        application.setStatus("INTERVIEWING");
        return application;
    }

    private MessageConversationCreateDTO createDTO(Long applicationId) {
        MessageConversationCreateDTO dto =
                new MessageConversationCreateDTO();
        dto.setApplicationId(applicationId);
        return dto;
    }
}

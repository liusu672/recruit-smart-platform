package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.MessageConversationCreateDTO;
import com.recruit.biz.dto.MessagePageQueryDTO;
import com.recruit.biz.dto.MessageSendDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.Interview;
import com.recruit.biz.entity.JobApplication;
import com.recruit.biz.entity.JobPosition;
import com.recruit.biz.entity.MessageConversation;
import com.recruit.biz.entity.MessageConversationMember;
import com.recruit.biz.entity.MessageRecord;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.MessageType;
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
import com.recruit.biz.service.MessageService;
import com.recruit.biz.service.MessageRealtimeService;
import com.recruit.biz.vo.MessageConversationSummaryVO;
import com.recruit.biz.vo.MessageRecordVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageConversationMapper conversationMapper;

    @Resource
    private MessageConversationMemberMapper memberMapper;

    @Resource
    private MessageRecordMapper messageRecordMapper;

    @Resource
    private JobApplicationMapper jobApplicationMapper;

    @Resource
    private JobPositionMapper jobPositionMapper;

    @Resource
    private CandidateMapper candidateMapper;

    @Resource
    private InterviewMapper interviewMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private MessageRealtimeService messageRealtimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long getOrCreateConversation(MessageConversationCreateDTO dto) {
        JobApplication application = requireApplication(dto.getApplicationId());
        checkApplicationAccess(application);
        MessageConversation conversation = findOrCreateConversation(application);
        ensureCurrentMember(conversation.getId());
        return conversation.getId();
    }

    @Override
    public PageResult<MessageConversationSummaryVO> listConversations(
            MessagePageQueryDTO dto
    ) {
        MessagePageQueryDTO query = dto == null
                ? new MessagePageQueryDTO()
                : dto;
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        CurrentUser currentUser = UserContext.getRequired();
        Set<Long> applicationIds = accessibleApplicationIds(currentUser);
        if (applicationIds != null && applicationIds.isEmpty()) {
            return new PageResult<>(0L, List.of());
        }

        LambdaQueryWrapper<MessageConversation> wrapper =
                new LambdaQueryWrapper<MessageConversation>()
                        .orderByDesc(MessageConversation::getLastMessageAt)
                        .orderByDesc(MessageConversation::getId);
        if (applicationIds != null) {
            wrapper.in(MessageConversation::getApplicationId, applicationIds);
        }

        Page<MessageConversation> result = conversationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );
        return new PageResult<>(
                result.getTotal(),
                toConversationSummaries(result.getRecords(), currentUser)
        );
    }

    @Override
    public PageResult<MessageRecordVO> listMessages(
            Long conversationId,
            MessagePageQueryDTO dto
    ) {
        MessageConversation conversation = requireConversation(conversationId);
        JobApplication application = requireApplication(
                conversation.getApplicationId()
        );
        checkApplicationAccess(application);

        MessagePageQueryDTO query = dto == null
                ? new MessagePageQueryDTO()
                : dto;
        Page<MessageRecord> result = messageRecordMapper.selectPage(
                new Page<>(
                        normalizePageNum(query.getPageNum()),
                        normalizePageSize(query.getPageSize())
                ),
                new LambdaQueryWrapper<MessageRecord>()
                        .eq(
                                MessageRecord::getConversationId,
                                conversationId
                        )
                        .orderByDesc(MessageRecord::getId)
        );

        Set<Long> senderIds = result.getRecords().stream()
                .map(MessageRecord::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = senderIds.isEmpty()
                ? Map.of()
                : sysUserMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Long currentUserId = UserContext.getUserId();
        List<MessageRecordVO> records = result.getRecords().stream()
                .map(message -> toMessageVO(message, userMap, currentUserId))
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long conversationId, MessageSendDTO dto) {
        MessageConversation conversation = requireConversation(conversationId);
        JobApplication application = requireApplication(
                conversation.getApplicationId()
        );
        checkApplicationAccess(application);
        ensureCurrentMember(conversationId);

        CurrentUser currentUser = UserContext.getRequired();
        String content = dto.getContent().trim();
        LocalDateTime now = LocalDateTime.now();
        MessageRecord message = new MessageRecord();
        message.setConversationId(conversationId);
        message.setSenderId(currentUser.getUserId());
        message.setSenderRole(currentUser.getRoleCode());
        message.setMessageType(MessageType.TEXT.name());
        message.setContent(content);
        message.setCreatedAt(now);
        messageRecordMapper.insert(message);

        int conversationUpdated = conversationMapper.update(
                null,
                new LambdaUpdateWrapper<MessageConversation>()
                        .eq(MessageConversation::getId, conversationId)
                        .set(
                                MessageConversation::getLastMessagePreview,
                                messagePreview(content)
                        )
                        .set(MessageConversation::getLastMessageAt, now)
        );
        if (conversationUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新消息会话失败"
            );
        }

        updateCurrentMemberReadAt(conversationId, now);
        publishChangedAfterCommit();
        return message.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long conversationId) {
        MessageConversation conversation = requireConversation(conversationId);
        JobApplication application = requireApplication(
                conversation.getApplicationId()
        );
        checkApplicationAccess(application);
        ensureCurrentMember(conversationId);
        updateCurrentMemberReadAt(conversationId, LocalDateTime.now());
        publishChangedAfterCommit();
    }

    @Override
    public Long countUnreadMessages() {
        CurrentUser currentUser = UserContext.getRequired();
        Set<Long> applicationIds = accessibleApplicationIds(currentUser);
        if (applicationIds != null && applicationIds.isEmpty()) {
            return 0L;
        }

        LambdaQueryWrapper<MessageConversation> wrapper =
                new LambdaQueryWrapper<>();
        if (applicationIds != null) {
            wrapper.in(MessageConversation::getApplicationId, applicationIds);
        }
        List<MessageConversation> conversations =
                conversationMapper.selectList(wrapper);
        if (conversations.isEmpty()) {
            return 0L;
        }

        Set<Long> conversationIds = conversations.stream()
                .map(MessageConversation::getId)
                .collect(Collectors.toSet());
        Map<Long, MessageConversationMember> memberMap = memberMapper.selectList(
                        new LambdaQueryWrapper<MessageConversationMember>()
                                .eq(
                                        MessageConversationMember::getUserId,
                                        currentUser.getUserId()
                                )
                                .in(
                                        MessageConversationMember::getConversationId,
                                        conversationIds
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        MessageConversationMember::getConversationId,
                        Function.identity()
                ));

        return conversations.stream()
                .mapToLong(conversation -> {
                    MessageConversationMember member = memberMap.get(
                            conversation.getId()
                    );
                    return countUnreadMessages(
                            conversation.getId(),
                            currentUser.getUserId(),
                            member == null ? null : member.getLastReadAt()
                    );
                })
                .sum();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendSystemMessage(Long applicationId, String content) {
        JobApplication application = requireApplication(applicationId);
        MessageConversation conversation = findOrCreateConversation(application);
        String normalizedContent = normalizeSystemContent(content);
        LocalDateTime now = LocalDateTime.now();

        MessageRecord message = new MessageRecord();
        message.setConversationId(conversation.getId());
        message.setSenderId(null);
        message.setSenderRole("SYSTEM");
        message.setMessageType(MessageType.SYSTEM.name());
        message.setContent(normalizedContent);
        message.setCreatedAt(now);
        messageRecordMapper.insert(message);

        int updated = conversationMapper.update(
                null,
                new LambdaUpdateWrapper<MessageConversation>()
                        .eq(
                                MessageConversation::getId,
                                conversation.getId()
                        )
                        .set(
                                MessageConversation::getLastMessagePreview,
                                messagePreview(normalizedContent)
                        )
                        .set(MessageConversation::getLastMessageAt, now)
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新系统消息会话失败"
            );
        }

        publishChangedAfterCommit();
        return message.getId();
    }

    private List<MessageConversationSummaryVO> toConversationSummaries(
            List<MessageConversation> conversations,
            CurrentUser currentUser
    ) {
        if (conversations.isEmpty()) {
            return List.of();
        }

        Set<Long> applicationIds = conversations.stream()
                .map(MessageConversation::getApplicationId)
                .collect(Collectors.toSet());
        Map<Long, JobApplication> applicationMap = jobApplicationMapper
                .selectBatchIds(applicationIds)
                .stream()
                .collect(Collectors.toMap(
                        JobApplication::getId,
                        Function.identity()
                ));
        Set<Long> jobIds = applicationMap.values().stream()
                .map(JobApplication::getJobId)
                .collect(Collectors.toSet());
        Set<Long> candidateIds = applicationMap.values().stream()
                .map(JobApplication::getCandidateId)
                .collect(Collectors.toSet());
        Map<Long, JobPosition> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobPositionMapper.selectBatchIds(jobIds).stream()
                .collect(Collectors.toMap(
                        JobPosition::getId,
                        Function.identity()
                ));
        Map<Long, Candidate> candidateMap = candidateIds.isEmpty()
                ? Map.of()
                : candidateMapper.selectBatchIds(candidateIds).stream()
                .collect(Collectors.toMap(
                        Candidate::getId,
                        Function.identity()
                ));
        Set<Long> conversationIds = conversations.stream()
                .map(MessageConversation::getId)
                .collect(Collectors.toSet());
        Map<Long, MessageConversationMember> memberMap = memberMapper.selectList(
                        new LambdaQueryWrapper<MessageConversationMember>()
                                .eq(
                                        MessageConversationMember::getUserId,
                                        currentUser.getUserId()
                                )
                                .in(
                                        MessageConversationMember::getConversationId,
                                        conversationIds
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        MessageConversationMember::getConversationId,
                        Function.identity()
                ));

        return conversations.stream()
                .map(conversation -> toConversationSummary(
                        conversation,
                        applicationMap,
                        jobMap,
                        candidateMap,
                        memberMap.get(conversation.getId()),
                        currentUser.getUserId()
                ))
                .toList();
    }

    private MessageConversationSummaryVO toConversationSummary(
            MessageConversation conversation,
            Map<Long, JobApplication> applicationMap,
            Map<Long, JobPosition> jobMap,
            Map<Long, Candidate> candidateMap,
            MessageConversationMember member,
            Long currentUserId
    ) {
        JobApplication application = applicationMap.get(
                conversation.getApplicationId()
        );
        JobPosition job = application == null
                ? null
                : jobMap.get(application.getJobId());
        Candidate candidate = application == null
                ? null
                : candidateMap.get(application.getCandidateId());

        MessageConversationSummaryVO vo = new MessageConversationSummaryVO();
        vo.setId(conversation.getId());
        vo.setApplicationId(conversation.getApplicationId());
        if (application != null) {
            vo.setJobId(application.getJobId());
            vo.setCandidateId(application.getCandidateId());
            vo.setApplicationStatus(application.getStatus());
        }
        vo.setJobTitle(job == null ? null : job.getTitle());
        vo.setCandidateName(candidate == null ? null : candidate.getName());
        vo.setLastMessagePreview(conversation.getLastMessagePreview());
        vo.setLastMessageAt(conversation.getLastMessageAt());
        vo.setUnreadCount(countUnreadMessages(
                conversation.getId(),
                currentUserId,
                member == null ? null : member.getLastReadAt()
        ));
        vo.setCreatedAt(conversation.getCreatedAt());
        return vo;
    }

    private long countUnreadMessages(
            Long conversationId,
            Long currentUserId,
            LocalDateTime lastReadAt
    ) {
        return messageRecordMapper.selectCount(
                new LambdaQueryWrapper<MessageRecord>()
                        .eq(MessageRecord::getConversationId, conversationId)
                        .and(condition -> condition
                                .ne(MessageRecord::getSenderId, currentUserId)
                                .or()
                                .isNull(MessageRecord::getSenderId)
                        )
                        .gt(
                                lastReadAt != null,
                                MessageRecord::getCreatedAt,
                                lastReadAt
                        )
        );
    }

    private MessageRecordVO toMessageVO(
            MessageRecord message,
            Map<Long, SysUser> userMap,
            Long currentUserId
    ) {
        SysUser sender = message.getSenderId() == null
                ? null
                : userMap.get(message.getSenderId());
        MessageRecordVO vo = new MessageRecordVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setSenderId(message.getSenderId());
        vo.setSenderName(
                MessageType.SYSTEM.name().equals(message.getMessageType())
                        ? "系统消息"
                        : sender == null ? null : sender.getRealName()
        );
        vo.setSenderRole(message.getSenderRole());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getContent());
        vo.setMine(currentUserId.equals(message.getSenderId()));
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private Set<Long> accessibleApplicationIds(CurrentUser currentUser) {
        String roleCode = currentUser.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return null;
        }
        if ("CANDIDATE".equals(roleCode)) {
            Candidate candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(Candidate::getUserId, currentUser.getUserId())
            );
            if (candidate == null) {
                return Set.of();
            }
            return jobApplicationMapper.selectList(
                            new LambdaQueryWrapper<JobApplication>()
                                    .eq(
                                            JobApplication::getCandidateId,
                                            candidate.getId()
                                    )
                    )
                    .stream()
                    .map(JobApplication::getId)
                    .collect(Collectors.toSet());
        }
        if ("INTERVIEWER".equals(roleCode)) {
            return interviewMapper.selectList(
                            new LambdaQueryWrapper<Interview>()
                                    .eq(
                                            Interview::getInterviewerId,
                                            currentUser.getUserId()
                                    )
                    )
                    .stream()
                    .map(Interview::getApplicationId)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    private void checkApplicationAccess(JobApplication application) {
        CurrentUser currentUser = UserContext.getRequired();
        String roleCode = currentUser.getRoleCode();
        if ("ADMIN".equals(roleCode) || "HR".equals(roleCode)) {
            return;
        }
        if ("CANDIDATE".equals(roleCode)) {
            Candidate candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(Candidate::getUserId, currentUser.getUserId())
            );
            if (candidate != null
                    && candidate.getId().equals(application.getCandidateId())) {
                return;
            }
        }
        if ("INTERVIEWER".equals(roleCode)) {
            Long interviewCount = interviewMapper.selectCount(
                    new LambdaQueryWrapper<Interview>()
                            .eq(
                                    Interview::getApplicationId,
                                    application.getId()
                            )
                            .eq(
                                    Interview::getInterviewerId,
                                    currentUser.getUserId()
                            )
            );
            if (interviewCount > 0) {
                return;
            }
        }
        throw new BusinessException(
                ErrorCode.FORBIDDEN,
                "无权访问该投递消息会话"
        );
    }

    private void ensureCurrentMember(Long conversationId) {
        CurrentUser currentUser = UserContext.getRequired();
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<MessageConversationMember>()
                        .eq(
                                MessageConversationMember::getConversationId,
                                conversationId
                        )
                        .eq(
                                MessageConversationMember::getUserId,
                                currentUser.getUserId()
                        )
        );
        if (count > 0) {
            return;
        }

        MessageConversationMember member = new MessageConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(currentUser.getUserId());
        member.setRoleCode(currentUser.getRoleCode());
        try {
            memberMapper.insert(member);
        } catch (DuplicateKeyException e) {
            Long existingCount = memberMapper.selectCount(
                    new LambdaQueryWrapper<MessageConversationMember>()
                            .eq(
                                    MessageConversationMember::getConversationId,
                                    conversationId
                            )
                            .eq(
                                    MessageConversationMember::getUserId,
                                    currentUser.getUserId()
                            )
            );
            if (existingCount == 0) {
                throw e;
            }
        }
    }

    private void updateCurrentMemberReadAt(
            Long conversationId,
            LocalDateTime readAt
    ) {
        int updated = memberMapper.update(
                null,
                new LambdaUpdateWrapper<MessageConversationMember>()
                        .eq(
                                MessageConversationMember::getConversationId,
                                conversationId
                        )
                        .eq(
                                MessageConversationMember::getUserId,
                                UserContext.getUserId()
                        )
                        .set(MessageConversationMember::getLastReadAt, readAt)
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "更新消息已读状态失败"
            );
        }
    }

    private MessageConversation requireConversation(Long id) {
        MessageConversation conversation = conversationMapper.selectById(id);
        if (conversation == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "消息会话不存在"
            );
        }
        return conversation;
    }

    private MessageConversation findOrCreateConversation(
            JobApplication application
    ) {
        MessageConversation existing = conversationMapper.selectOne(
                new LambdaQueryWrapper<MessageConversation>()
                        .eq(
                                MessageConversation::getApplicationId,
                                application.getId()
                        )
        );
        if (existing != null) {
            return existing;
        }

        CurrentUser currentUser = UserContext.get();
        MessageConversation conversation = new MessageConversation();
        conversation.setApplicationId(application.getId());
        conversation.setCreatedBy(
                currentUser == null ? null : currentUser.getUserId()
        );
        try {
            conversationMapper.insert(conversation);
            return conversation;
        } catch (DuplicateKeyException e) {
            MessageConversation concurrentConversation =
                    conversationMapper.selectOne(
                            new LambdaQueryWrapper<MessageConversation>()
                                    .eq(
                                            MessageConversation::getApplicationId,
                                            application.getId()
                                    )
                    );
            if (concurrentConversation == null) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "创建消息会话失败"
                );
            }
            return concurrentConversation;
        }
    }

    private JobApplication requireApplication(Long id) {
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "投递记录不存在"
            );
        }
        return application;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 || pageSize > 100
                ? 20
                : pageSize;
    }

    private String messagePreview(String content) {
        return content.length() <= 255 ? content : content.substring(0, 255);
    }

    private String normalizeSystemContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "系统消息内容不能为空"
            );
        }
        String normalized = content.trim();
        return normalized.length() <= 5000
                ? normalized
                : normalized.substring(0, 5000);
    }

    private void publishChangedAfterCommit() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            messageRealtimeService.publishChanged();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messageRealtimeService.publishChanged();
                    }
                }
        );
    }
}

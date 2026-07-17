package com.recruit.biz.service.impl;

import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.enums.ProcessEventSource;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
import com.recruit.biz.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationProcessEventServiceImpl
        implements ApplicationProcessEventService {

    @Resource
    private ApplicationProcessEventMapper applicationProcessEventMapper;

    @Resource
    private MessageService messageService;

    @Override
    public void record(
            Long applicationId,
            ProcessEventType eventType,
            String fromStatus,
            String toStatus,
            String description,
            ProcessRelatedType relatedType,
            Long relatedId
    ) {
        record(
                applicationId,
                eventType,
                fromStatus,
                toStatus,
                description,
                ProcessEventSource.BUSINESS,
                relatedType,
                relatedId
        );
    }

    @Override
    public void record(
            Long applicationId,
            ProcessEventType eventType,
            String fromStatus,
            String toStatus,
            String description,
            ProcessEventSource sourceType,
            ProcessRelatedType relatedType,
            Long relatedId
    ) {
        CurrentUser currentUser = UserContext.get();
        ApplicationProcessEvent event = new ApplicationProcessEvent();
        event.setApplicationId(applicationId);
        event.setEventType(eventType.name());
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setTitle(eventType.getTitle());
        event.setDescription(description);
        if (currentUser != null) {
            event.setOperatorId(currentUser.getUserId());
            event.setOperatorRole(currentUser.getRoleCode());
        }
        event.setSourceType(sourceType.name());
        event.setRelatedType(relatedType.name());
        event.setRelatedId(relatedId);
        event.setOccurredAt(LocalDateTime.now());
        applicationProcessEventMapper.insert(event);

        String notification = notificationContent(eventType);
        if (notification != null) {
            messageService.sendSystemMessage(applicationId, notification);
        }
    }

    private String notificationContent(ProcessEventType eventType) {
        return switch (eventType) {
            case APPLICATION_SUBMITTED -> "职位投递已成功提交。";
            case APPLICATION_WITHDRAWN -> "候选人已撤回该职位投递。";
            case SCREENING_PASSED -> "简历初筛已通过，后续将安排面试。";
            case SCREENING_REJECTED, APPLICATION_REJECTED ->
                    "本次招聘流程未能继续推进。";
            case INTERVIEW_ASSIGNED ->
                    "面试官已被指派，正在等待确认面试时间。";
            case INTERVIEW_SCHEDULED ->
                    "面试时间已经确认，请查看最新面试安排。";
            case INTERVIEW_UPDATED ->
                    "面试安排已更新，请查看最新信息。";
            case INTERVIEW_CANCELED -> "本轮面试已取消。";
            case INTERVIEW_COMPLETED -> "本轮面试已完成。";
            case OFFER_SENT -> "Offer 已发送，请及时查看并处理。";
            case OFFER_ACCEPTED -> "候选人已接受 Offer。";
            case OFFER_REJECTED -> "候选人已拒绝 Offer。";
            case OFFER_REVOKED -> "Offer 已撤回。";
            case ONBOARDING_CREATED -> "入职流程已创建，请提交入职材料。";
            case MATERIAL_SUBMITTED -> "候选人已提交入职材料。";
            case MATERIAL_APPROVED -> "入职材料审核已通过。";
            case MATERIAL_REJECTED -> "入职材料审核未通过，请修改后重新提交。";
            case ONBOARDING_COMPLETED -> "入职流程已完成。";
            case ONBOARDING_CANCELED -> "入职流程已取消。";
            default -> null;
        };
    }
}

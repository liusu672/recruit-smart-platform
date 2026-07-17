package com.recruit.biz.service.impl;

import com.recruit.biz.entity.ApplicationProcessEvent;
import com.recruit.biz.enums.ProcessEventSource;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;
import com.recruit.biz.mapper.ApplicationProcessEventMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.ApplicationProcessEventService;
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
    }
}

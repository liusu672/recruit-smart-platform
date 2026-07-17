package com.recruit.biz.service;

import com.recruit.biz.enums.ProcessEventSource;
import com.recruit.biz.enums.ProcessEventType;
import com.recruit.biz.enums.ProcessRelatedType;

public interface ApplicationProcessEventService {

    void record(
            Long applicationId,
            ProcessEventType eventType,
            String fromStatus,
            String toStatus,
            String description,
            ProcessRelatedType relatedType,
            Long relatedId
    );

    void record(
            Long applicationId,
            ProcessEventType eventType,
            String fromStatus,
            String toStatus,
            String description,
            ProcessEventSource sourceType,
            ProcessRelatedType relatedType,
            Long relatedId
    );
}

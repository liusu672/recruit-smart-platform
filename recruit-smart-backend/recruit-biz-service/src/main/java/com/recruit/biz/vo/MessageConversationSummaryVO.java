package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageConversationSummaryVO {
    private Long id;
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String applicationStatus;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private LocalDateTime createdAt;
}

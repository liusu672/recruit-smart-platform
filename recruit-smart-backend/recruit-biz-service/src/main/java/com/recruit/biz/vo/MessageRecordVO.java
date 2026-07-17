package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageRecordVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String messageType;
    private String content;
    private Boolean mine;
    private LocalDateTime createdAt;
}

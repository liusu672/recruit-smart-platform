package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_conversation")
public class MessageConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

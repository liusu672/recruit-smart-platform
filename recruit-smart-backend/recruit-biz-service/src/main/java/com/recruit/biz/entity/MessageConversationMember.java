package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_conversation_member")
public class MessageConversationMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long userId;
    private String roleCode;
    private LocalDateTime lastReadAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

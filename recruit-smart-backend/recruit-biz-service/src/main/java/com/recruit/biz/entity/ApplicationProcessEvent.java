package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application_process_event")
public class ApplicationProcessEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String title;
    private String description;
    private Long operatorId;
    private String operatorRole;
    private String sourceType;
    private String relatedType;
    private Long relatedId;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}

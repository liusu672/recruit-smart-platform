package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview")
public class Interview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Long interviewerId;
    private String round;
    private LocalDateTime interviewTime;
    private String method;
    private String location;
    private String status;
    private Long createdBy;
    private LocalDateTime assignedAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

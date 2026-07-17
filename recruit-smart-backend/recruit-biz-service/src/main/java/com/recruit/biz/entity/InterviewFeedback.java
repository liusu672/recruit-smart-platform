package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_feedback")
public class InterviewFeedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long interviewId;
    private Long interviewerId;
    private String state;
    private String scorecardJson;
    private Integer score;
    private String comment;
    private String suggestion;
    private String aiSummary;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

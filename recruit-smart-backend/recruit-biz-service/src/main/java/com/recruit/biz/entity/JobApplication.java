package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_application")
public class JobApplication {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long resumeId;
    private String status;
    private Integer allowAdjustment;
    private Long adjustedJobId;
    private String source;
    private String hrNote;
    private String rejectReasonCode;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

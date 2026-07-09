package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("onboarding")
public class Onboarding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long offerId;
    private Long candidateId;
    private String status;
    private String currentStep;
    private String materialStatus;
    private String remark;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

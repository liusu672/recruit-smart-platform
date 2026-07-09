package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("offer")
public class Offer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private BigDecimal salary;
    private LocalDate entryDate;
    private Integer probationMonths;
    private String workLocation;
    private String status;
    private String remark;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

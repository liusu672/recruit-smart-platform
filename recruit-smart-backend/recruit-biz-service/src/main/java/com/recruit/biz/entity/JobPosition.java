package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("job_position")
public class JobPosition {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String title;
    private String department;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Integer headcount;
    private String responsibilities;
    private String requirements;
    private String status;
    private Long createdBy;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("candidate")
public class Candidate {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String education;
    private String school;
    private String major;
    private Integer yearsOfExperience;
    private String currentStatus;
    private String source;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

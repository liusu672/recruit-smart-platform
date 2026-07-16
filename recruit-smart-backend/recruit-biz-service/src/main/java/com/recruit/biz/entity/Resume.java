package com.recruit.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long candidateId;
    private String resumeName;
    private String fileUrl;
    private String fileType;
    private String parsedContent;
    private String skills;
    private String projectExperience;
    private String workExperience;
    private Integer isDefault;
    private String parseStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

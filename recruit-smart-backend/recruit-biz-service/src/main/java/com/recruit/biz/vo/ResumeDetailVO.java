package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeDetailVO {
    private Long id;

    private Long candidateId;

    private String resumeName;

    private String fileUrl;

    private String fileType;

    private Integer isDefault;

    private String parseStatus;

    private String parseStatusText;

    private String parsedContent;

    private String skills;

    private String projectExperience;

    private String workExperience;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

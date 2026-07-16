package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeSummaryVO {

    private Long id;

    private String resumeName;

    private String fileUrl;

    private String fileType;

    private Integer isDefault;

    private LocalDateTime createdAt;

    private String parseStatus;

    private String parseStatusText;
}

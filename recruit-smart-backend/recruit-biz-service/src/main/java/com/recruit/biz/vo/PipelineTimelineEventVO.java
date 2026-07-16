package com.recruit.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineTimelineEventVO {
    private String id;
    private String title;
    private String description;
    private String actorName;
    private LocalDateTime occurredAt;
    private String source;
    private String relatedObject;
}

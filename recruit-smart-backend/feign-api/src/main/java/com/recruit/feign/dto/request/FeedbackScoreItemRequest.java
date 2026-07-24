package com.recruit.feign.dto.request;

import lombok.Data;

@Data
public class FeedbackScoreItemRequest {
    private String label;
    private String description;
    private Integer score;
    private String evidence;
}

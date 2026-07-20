package com.recruit.feign.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ResumeMatchResponse {
    private Integer score;
    private String level;
    private String summary;
    private List<String> matchedPoints;
    private List<String> riskPoints;
    private String suggestion;
}

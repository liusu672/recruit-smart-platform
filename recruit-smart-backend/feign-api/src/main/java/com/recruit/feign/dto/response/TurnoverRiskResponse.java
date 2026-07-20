package com.recruit.feign.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TurnoverRiskResponse {
    private String riskLevel;
    private Integer riskScore;
    private String summary;
    private List<String> riskReasons;
    private List<String> suggestions;
}

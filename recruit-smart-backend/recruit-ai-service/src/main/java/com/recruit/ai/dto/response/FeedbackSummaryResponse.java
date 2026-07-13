package com.recruit.ai.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class FeedbackSummaryResponse {
    private String summary;
    private List<String> advantages;
    private List<String> risks;
    private String suggestion;
}

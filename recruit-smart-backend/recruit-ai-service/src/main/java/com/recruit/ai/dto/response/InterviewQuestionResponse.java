package com.recruit.ai.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class InterviewQuestionResponse {
    private String category;
    private String summary;
    private List<String> questions;
}

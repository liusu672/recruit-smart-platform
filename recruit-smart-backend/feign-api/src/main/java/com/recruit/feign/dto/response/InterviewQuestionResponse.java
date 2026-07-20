package com.recruit.feign.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class InterviewQuestionResponse {
    private String category;
    private String summary;
    private List<String> questions;
}

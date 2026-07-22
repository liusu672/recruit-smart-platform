package com.recruit.feign.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewQuestionResponse {
    private String category;
    private String summary;
    private List<InterviewQuestionItemResponse> questions;
}

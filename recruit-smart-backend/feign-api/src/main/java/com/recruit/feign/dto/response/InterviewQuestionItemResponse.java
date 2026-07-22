package com.recruit.feign.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewQuestionItemResponse {

    private String title;
    private String content;
    private List<String> focus;
    private String difficulty;
    private String referenceAnswer;
    private List<String> answerPoints;
}

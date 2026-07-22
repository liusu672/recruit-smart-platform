package com.recruit.feign.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ResumeParseResponse {
    private String parsedContent;
    private List<String> skills;
    private String projectExperience;
    private String workExperience;
    private String education;
    private String school;
    private String major;
    private String summary;
    private List<String> warnings;
}

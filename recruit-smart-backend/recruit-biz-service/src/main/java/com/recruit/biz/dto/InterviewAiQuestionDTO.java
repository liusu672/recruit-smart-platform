package com.recruit.biz.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterviewAiQuestionDTO {
    @Size(max = 500, message = "补充追问主题不能超过500个字符")
    private String focus;
}

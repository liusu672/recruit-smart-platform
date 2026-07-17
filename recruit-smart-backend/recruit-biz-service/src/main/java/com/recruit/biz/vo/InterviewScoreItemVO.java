package com.recruit.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScoreItemVO {
    private String key;
    private String label;
    private String description;
    private Integer score;
    private String evidence;
}

package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CandidateStatus {
    AVAILABLE("可应聘"),
    INTERVIEWING("面试中"),
    HIRED("已入职");

    private final String description;
}

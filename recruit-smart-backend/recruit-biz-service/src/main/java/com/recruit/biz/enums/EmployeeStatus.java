package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmployeeStatus {
    PROBATION("试用期"),
    ACTIVE("在职"),
    LEFT("已离职");

    private final String description;

    public static EmployeeStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return EmployeeStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

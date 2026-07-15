package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeSummaryVO {
    private Long id;
    private Long candidateId;
    private String employeeNo;
    private String name;
    private String phone;
    private String email;
    private String department;
    private String position;
    private LocalDate entryDate;
    private String status;
    private String statusText;
}

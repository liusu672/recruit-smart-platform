package com.recruit.biz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobPositionUpdateDTO {

    @NotBlank(message = "职位名称不能为空")
    private String title;

    @NotBlank(message = "部门不能为空")
    private String department;

    private String location;

    @NotNull(message = "最低薪资不能为空")
    private BigDecimal salaryMin;

    @NotNull(message = "最高薪资不能为空")
    private BigDecimal salaryMax;

    @NotNull(message = "人数需求不能为空")
    @Min(value = 1, message = "最少需要招收1人")
    private Integer headcount;

    private String responsibilities;

    private String requirements;
}
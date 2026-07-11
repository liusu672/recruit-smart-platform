package com.recruit.biz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Data
public class JobPositionCreateDTO {
    @NotBlank(message="职务名称不能为空")
    private String title;
    @NotBlank(message="部门不能为空")
    private String department;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    @NotNull(message="人数需求不能为空")
    @Min(value=1,message="最少需要招收1人")
    private Integer headcount;
    private String responsibilities;
    private String requirements;
    @NotNull(message="创建者不能为空")
    private Long createdBy;
}

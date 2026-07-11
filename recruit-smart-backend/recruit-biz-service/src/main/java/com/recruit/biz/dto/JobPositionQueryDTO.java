package com.recruit.biz.dto;

import lombok.Data;

@Data
public class JobPositionQueryDTO {
    private String keyword;
    private String department;
    private String status;
    private Integer pageNum=1;
    private Integer pageSize=10;
}

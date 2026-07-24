package com.recruit.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PipelineStageCountVO {
    private String stage;
    private Long count;
}

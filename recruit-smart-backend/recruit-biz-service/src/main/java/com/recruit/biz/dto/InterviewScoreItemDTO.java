package com.recruit.biz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterviewScoreItemDTO {

    @NotBlank(message = "评分项编码不能为空")
    @Size(max = 64, message = "评分项编码不能超过64个字符")
    private String key;

    @NotBlank(message = "评分项名称不能为空")
    @Size(max = 64, message = "评分项名称不能超过64个字符")
    private String label;

    @Size(max = 500, message = "评分项说明不能超过500个字符")
    private String description;

    @Min(value = 1, message = "单项评分不能小于1")
    @Max(value = 4, message = "单项评分不能大于4")
    private Integer score;

    @Size(max = 1000, message = "评价证据不能超过1000个字符")
    private String evidence;
}

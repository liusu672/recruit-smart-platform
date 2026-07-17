package com.recruit.biz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class InterviewFeedbackDraftDTO {

    @NotNull(message = "评分卡不能为空")
    @Size(max = 20, message = "评分项不能超过20项")
    private List<@Valid InterviewScoreItemDTO> scorecard;

    @Min(value = 0, message = "面试评分不能小于0")
    @Max(value = 100, message = "面试评分不能大于100")
    private Integer score;

    @Size(max = 2000, message = "面试评价不能超过2000个字符")
    private String comment;

    @Pattern(
            regexp = "^(PASS|REJECT|PENDING)$",
            message = "录用建议不正确"
    )
    private String suggestion;
}

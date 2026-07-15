package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "提交面试反馈参数")
public class InterviewFeedbackCreateDTO {

    @NotNull(message = "面试评分不能为空")
    @Min(value = 0, message = "面试评分不能小于0")
    @Max(value = 100, message = "面试评分不能大于100")
    @Schema(description = "面试评分，0至100", example = "86")
    private Integer score;

    @NotBlank(message = "面试评价不能为空")
    @Size(max = 2000, message = "面试评价不能超过2000个字符")
    @Schema(description = "面试评价")
    private String comment;

    @NotBlank(message = "录用建议不能为空")
    @Pattern(
            regexp = "^(PASS|REJECT|PENDING)$",
            message = "录用建议不正确"
    )
    @Schema(description = "录用建议", example = "PASS")
    private String suggestion;
}

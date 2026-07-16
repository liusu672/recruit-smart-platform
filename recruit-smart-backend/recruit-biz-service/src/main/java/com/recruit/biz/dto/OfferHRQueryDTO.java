package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "HR Offer列表查询参数")
public class OfferHRQueryDTO {

    @Schema(description = "Offer状态", example = "SENT")
    @Pattern(
            regexp = "^(DRAFT|SENT|ACCEPTED|REJECTED|REVOKED)$",
            message = "Offer状态不正确"
    )
    private String status;

    @Positive(message = "职位ID必须大于0")
    @Schema(description = "职位ID", example = "1")
    private Long jobId;

    @Size(max = 128, message = "候选人关键词不能超过128个字符")
    @Schema(description = "候选人关键词，可匹配姓名、手机号或邮箱")
    private String candidateKeyword;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

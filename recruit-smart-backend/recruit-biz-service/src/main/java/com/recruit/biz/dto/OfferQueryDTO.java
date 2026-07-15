package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "候选人Offer列表查询参数")
public class OfferQueryDTO {

    @Schema(description = "Offer状态", example = "SENT")
    @Pattern(
            regexp = "^(SENT|ACCEPTED|REJECTED|REVOKED)$",
            message = "Offer状态不正确"
    )
    private String status;

    @Schema(description = "当前页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}

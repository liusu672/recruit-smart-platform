package com.recruit.biz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "创建Offer草稿参数")
public class OfferCreateDTO {

    @NotNull(message = "投递记录不能为空")
    @Schema(description = "投递记录ID", example = "2")
    private Long applicationId;

    @NotNull(message = "录用薪资不能为空")
    @DecimalMin(value = "0.01", message = "录用薪资必须大于0")
    @Digits(integer = 8, fraction = 2, message = "录用薪资格式不正确")
    @Schema(description = "录用薪资", example = "12000.00")
    private BigDecimal salary;

    @NotNull(message = "预计入职日期不能为空")
    @FutureOrPresent(message = "预计入职日期不能早于当前日期")
    @Schema(description = "预计入职日期", example = "2026-08-01")
    private LocalDate entryDate;

    @Min(value = 0, message = "试用期不能小于0个月")
    @Max(value = 6, message = "试用期不能超过6个月")
    @Schema(description = "试用期月数", example = "3")
    private Integer probationMonths = 3;

    @NotBlank(message = "工作地点不能为空")
    @Size(max = 128, message = "工作地点不能超过128个字符")
    @Schema(description = "工作地点", example = "武汉")
    private String workLocation;

    @Size(max = 2000, message = "Offer备注不能超过2000个字符")
    @Schema(description = "Offer备注")
    private String remark;
}

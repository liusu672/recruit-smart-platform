package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobPositionVO {

    private Long id;

    /**
     * 职位名称
     */
    private String title;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 职位类型：FULL_TIME / PART_TIME / INTERN 等
     */
    private String jobType;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 招聘人数
     */
    private Integer headcount;

    /**
     * 要求完成的面试轮数，范围为1-3。
     */
    private Integer requiredInterviewRounds;

    /**
     * 经验要求
     */
    private String experienceRequirement;

    /**
     * 学历要求
     */
    private String educationRequirement;

    /**
     * 职位描述
     */
    private String description;

    /**
     * 任职要求
     */
    private String requirement;

    /**
     * 职位状态：OPEN / CLOSED / DRAFT
     */
    private String status;

    /**
     * 状态中文展示：招聘中 / 已关闭 / 草稿
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

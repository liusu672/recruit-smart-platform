package com.recruit.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProcessEventType {
    APPLICATION_SUBMITTED("候选人提交投递"),
    SCREENING_STARTED("开始筛选投递"),
    SCREENING_PASSED("筛选通过"),
    SCREENING_REJECTED("筛选拒绝"),
    SCREENING_PENDING("筛选待定"),
    APPLICATION_REJECTED("终止招聘流程"),
    APPLICATION_WITHDRAWN("候选人撤回投递"),
    INTERVIEW_CREATED("创建面试安排"),
    INTERVIEW_UPDATED("修改面试安排"),
    INTERVIEW_CANCELED("取消面试"),
    INTERVIEW_COMPLETED("完成面试"),
    INTERVIEW_FEEDBACK_SUBMITTED("提交面试反馈"),
    OFFER_CREATED("创建 Offer"),
    OFFER_UPDATED("修改 Offer 草稿"),
    OFFER_SENT("发送 Offer"),
    OFFER_ACCEPTED("候选人接受 Offer"),
    OFFER_REJECTED("候选人拒绝 Offer"),
    OFFER_REVOKED("HR 撤回 Offer"),
    ONBOARDING_CREATED("创建入职流程"),
    MATERIAL_SUBMITTED("候选人提交入职材料"),
    MATERIAL_APPROVED("入职材料审核通过"),
    MATERIAL_REJECTED("入职材料被驳回"),
    ONBOARDING_COMPLETED("完成入职"),
    ONBOARDING_CANCELED("取消入职流程"),
    AI_MATCH_COMPLETED("生成岗位匹配结果");

    private final String title;
}

package com.recruit.biz.service;

import com.recruit.biz.vo.OnboardingDetailVO;
import com.recruit.biz.dto.OnboardingMaterialRejectDTO;
import com.recruit.biz.dto.OnboardingCancelDTO;
import com.recruit.biz.dto.OnboardingQueryDTO;
import com.recruit.biz.vo.OnboardingHRSummaryVO;
import com.recruit.biz.vo.OnboardingSummaryVO;
import com.recruit.common.result.PageResult;

import java.util.List;

public interface OnboardingService {
    List<OnboardingSummaryVO> listMyOnboarding();
    OnboardingDetailVO getDetail(Long id);
    void submitMaterials(Long id);
    PageResult<OnboardingHRSummaryVO> listOnboarding(
            OnboardingQueryDTO dto
    );
    void approveMaterials(Long id);
    void rejectMaterials(Long id, OnboardingMaterialRejectDTO dto);
    void completeOnboarding(Long id);
    void cancelOnboarding(Long id, OnboardingCancelDTO dto);
}

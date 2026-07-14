package com.recruit.biz.service;

import com.recruit.biz.dto.CandidateCreateDTO;
import com.recruit.biz.dto.CandidateQueryDTO;
import com.recruit.biz.dto.CandidateUpdateDTO;
import com.recruit.biz.vo.CandidateVO;
import com.recruit.common.result.PageResult;

public interface CandidateService {
    Long createCandidate(CandidateCreateDTO dto);
    PageResult<CandidateVO> pageCandidate(CandidateQueryDTO dto);
    void updateCandidate(Long id,CandidateUpdateDTO dto);
}

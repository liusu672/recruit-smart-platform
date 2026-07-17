package com.recruit.biz.service;

import com.recruit.biz.dto.InterviewCreateDTO;
import com.recruit.biz.dto.InterviewQueryDTO;
import com.recruit.biz.dto.InterviewScheduleDTO;
import com.recruit.biz.dto.InterviewUpdateDTO;
import com.recruit.biz.vo.InterviewDetailVO;
import com.recruit.biz.vo.InterviewSummaryVO;
import com.recruit.common.result.PageResult;

public interface InterviewService {
    Long createInterview(InterviewCreateDTO dto);
    PageResult<InterviewSummaryVO> listMyCandidateInterviews(
            InterviewQueryDTO dto
    );
    PageResult<InterviewSummaryVO> listMyInterviewerInterviews(
            InterviewQueryDTO dto
    );
    InterviewDetailVO getDetail(Long id);
    void updateInterview(Long id, InterviewUpdateDTO dto);
    void scheduleInterview(Long id, InterviewScheduleDTO dto);
    void cancelInterview(Long id);
    void completeInterview(Long id);
}

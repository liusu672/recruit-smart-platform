package com.recruit.biz.service;

import com.recruit.biz.dto.InterviewTaskQueryDTO;
import com.recruit.biz.vo.InterviewTaskSummaryVO;
import com.recruit.biz.vo.InterviewWorkspaceVO;
import com.recruit.common.result.PageResult;

public interface InterviewWorkspaceService {
    PageResult<InterviewTaskSummaryVO> listTasks(InterviewTaskQueryDTO dto);

    InterviewWorkspaceVO getWorkspace(Long interviewId);
}

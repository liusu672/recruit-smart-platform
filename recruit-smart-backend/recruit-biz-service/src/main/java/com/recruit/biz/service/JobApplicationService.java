package com.recruit.biz.service;

import com.recruit.biz.dto.JobApplicationCreateDTO;
import com.recruit.biz.dto.JobApplicationHRQueryDTO;
import com.recruit.biz.dto.JobApplicationQueryDTO;
import com.recruit.biz.dto.JobApplicationRejectDTO;
import com.recruit.biz.dto.JobApplicationScreeningDTO;
import com.recruit.biz.dto.JobApplicationStatusUpdateDTO;
import com.recruit.biz.vo.JobApplicationSummaryVO;
import com.recruit.biz.vo.JobApplicationDetailVO;
import com.recruit.biz.vo.JobApplicationHRSummaryVO;
import com.recruit.common.result.PageResult;

public interface JobApplicationService {
    Long createApplication(JobApplicationCreateDTO dto);
    PageResult<JobApplicationSummaryVO> listMyApplications(
            JobApplicationQueryDTO dto
    );
    JobApplicationDetailVO getDetail(Long id);
    void withdraw(Long id);
    PageResult<JobApplicationHRSummaryVO> listJobApplications(
            Long jobId,
            JobApplicationHRQueryDTO dto
    );
    void reject(Long id, JobApplicationRejectDTO dto);
    void reviewScreening(Long id, JobApplicationScreeningDTO dto);
    void updateStatus(Long id, JobApplicationStatusUpdateDTO dto);
}

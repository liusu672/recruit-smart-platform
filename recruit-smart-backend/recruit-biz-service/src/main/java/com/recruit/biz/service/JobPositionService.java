package com.recruit.biz.service;

import com.recruit.biz.dto.JobPositionCreateDTO;
import com.recruit.biz.dto.JobPositionQueryDTO;
import com.recruit.biz.dto.JobPositionUpdateDTO;
import com.recruit.biz.vo.JobPositionVO;
import com.recruit.common.result.PageResult;


public interface JobPositionService {
    Long createJob(JobPositionCreateDTO dto);
    void updateJob(Long id, JobPositionUpdateDTO dto);
    JobPositionVO getById(Long id);
    PageResult<JobPositionVO> jobPages(JobPositionQueryDTO dto);
    JobPositionVO getOpenById(Long id);
    PageResult<JobPositionVO> openJobPages(JobPositionQueryDTO dto);
    void publishJob(Long id);
    void pauseJob(Long id);
    void resumeJob(Long id);
    void closeJob(Long id);
}

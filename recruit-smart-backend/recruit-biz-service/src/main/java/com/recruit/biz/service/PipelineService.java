package com.recruit.biz.service;

import com.recruit.biz.dto.PipelineApplicationQueryDTO;
import com.recruit.biz.vo.PipelineApplicationDetailVO;
import com.recruit.biz.vo.PipelineApplicationSummaryVO;
import com.recruit.biz.vo.PipelineStageCountVO;
import com.recruit.common.result.PageResult;

import java.util.List;

public interface PipelineService {
    PageResult<PipelineApplicationSummaryVO> listPipeline(
            PipelineApplicationQueryDTO dto
    );

    List<PipelineStageCountVO> listStageCounts(PipelineApplicationQueryDTO dto);

    PipelineApplicationDetailVO getPipelineDetail(Long applicationId);
}

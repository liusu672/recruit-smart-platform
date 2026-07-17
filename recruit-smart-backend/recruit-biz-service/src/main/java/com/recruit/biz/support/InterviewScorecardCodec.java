package com.recruit.biz.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.biz.vo.InterviewScoreItemVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InterviewScorecardCodec {

    private final ObjectMapper objectMapper;

    public String write(List<?> scorecard) {
        try {
            return objectMapper.writeValueAsString(scorecard);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "评分卡序列化失败"
            );
        }
    }

    public List<InterviewScoreItemVO> read(String scorecardJson) {
        if (scorecardJson == null || scorecardJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    scorecardJson,
                    new TypeReference<List<InterviewScoreItemVO>>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "评分卡数据格式不正确"
            );
        }
    }
}

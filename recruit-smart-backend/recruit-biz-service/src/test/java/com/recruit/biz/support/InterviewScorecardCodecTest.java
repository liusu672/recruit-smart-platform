package com.recruit.biz.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.biz.vo.InterviewScoreItemVO;
import com.recruit.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterviewScorecardCodecTest {

    private final InterviewScorecardCodec codec =
            new InterviewScorecardCodec(new ObjectMapper());

    @Test
    void readsCurrentArrayScorecard() {
        List<InterviewScoreItemVO> result = codec.read("""
                [{"key":"professional","label":"专业能力",
                "description":"核心能力","score":4,"evidence":"项目完整"}]
                """);

        assertEquals(1, result.size());
        assertEquals("professional", result.get(0).getKey());
        assertEquals(4, result.get(0).getScore());
    }

    @Test
    void convertsLegacyObjectScorecardToCurrentItems() {
        List<InterviewScoreItemVO> result = codec.read("""
                {"专业能力":5,"团队协作":5,"岗位动机":4}
                """);

        assertEquals(3, result.size());
        assertEquals("professional", result.get(0).getKey());
        assertEquals(4, result.get(0).getScore());
        assertEquals("团队协作", result.get(1).getLabel());
        assertEquals(3, result.get(2).getScore());
        assertEquals("历史数据未记录评价证据", result.get(0).getEvidence());
    }

    @Test
    void rejectsUnsupportedScorecardShape() {
        assertThrows(
                BusinessException.class,
                () -> codec.read("{\"专业能力\":\"优秀\"}")
        );
    }
}

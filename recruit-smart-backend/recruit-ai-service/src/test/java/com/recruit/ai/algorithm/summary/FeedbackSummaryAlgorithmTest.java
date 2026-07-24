package com.recruit.ai.algorithm.summary;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.response.FeedbackSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackSummaryAlgorithmTest {

    private FeedbackSummaryAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new FeedbackSummaryAlgorithm();
    }

    @Test
    void generate_withTypicalFeedback_extractsAdvantagesAndRisks() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setCandidateName("张三");
        request.setJobTitle("Java开发");
        request.setFeedbackText("该候选人的基础扎实，表达清晰流畅。但项目深度不足，部分问题回答不清晰。");
        request.setScore(75);

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertFalse(response.getAdvantages().isEmpty());
        assertFalse(response.getRisks().isEmpty());
        assertTrue(response.getAdvantages().stream().anyMatch(a -> a.contains("基础")));
        assertTrue(response.getAdvantages().stream().anyMatch(a -> a.contains("表达")));
        assertTrue(response.getRisks().stream().anyMatch(r -> r.contains("项目深度")));
        assertTrue(response.getRisks().stream().anyMatch(r -> r.contains("清晰")));
        assertTrue(response.getSuggestion().contains("下一轮"));
    }

    @Test
    void generate_withAllAdvantages_noRisks() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setCandidateName("李四");
        request.setJobTitle("产品经理");
        request.setFeedbackText("基础扎实，表达清晰，项目经验丰富，思路清晰，学习能力强，责任心强。");
        request.setScore(90);

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertEquals(6, response.getAdvantages().size());
        assertTrue(response.getRisks().isEmpty());
        assertTrue(response.getSuggestion().contains("录用"));
    }

    @Test
    void generate_withAllRisks_noAdvantages() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setCandidateName("王五");
        request.setJobTitle("测试");
        request.setFeedbackText("基础薄弱，经验不足，回答不清晰，项目深度不足。");
        request.setScore(40);

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertTrue(response.getAdvantages().isEmpty());
        assertFalse(response.getRisks().isEmpty());
        assertTrue(response.getSuggestion().contains("不建议录用"));
    }

    @Test
    void generate_withNullScoreAndMoreAdvantages_returnsNextRound() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("基础扎实，表达清晰，项目经验丰富。稳定性不足。");

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertNull(request.getScore());
        assertTrue(response.getAdvantages().size() > response.getRisks().size());
        assertTrue(response.getSuggestion().contains("下一轮"));
    }

    @Test
    void generate_withNullScoreAndEmptyFeedback_returnsInsufficient() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("");

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertTrue(response.getAdvantages().isEmpty());
        assertTrue(response.getRisks().isEmpty());
        assertTrue(response.getSuggestion().contains("信息不足"));
    }

    @Test
    void generate_withNullScoreAndMoreRisks_returnsCautious() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("经验不足，基础薄弱，项目深度不足。表达清晰。");

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertTrue(response.getRisks().size() > response.getAdvantages().size());
        assertTrue(response.getSuggestion().contains("谨慎评估"));
    }

    @Test
    void generate_withNullCandidateName_usesDefault() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("基础扎实");
        request.setScore(85);

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertTrue(response.getSummary().contains("候选人"));
    }

    @Test
    void generate_summaryContainsAllParts() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setCandidateName("赵六");
        request.setJobTitle("前端开发");
        request.setFeedbackText("基础扎实");
        request.setScore(85);

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertTrue(response.getSummary().contains("赵六"));
        assertTrue(response.getSummary().contains("前端开发"));
        assertTrue(response.getSummary().contains("优势"));
        assertTrue(response.getSummary().contains("风险点"));
        assertTrue(response.getSummary().contains("建议"));
    }

    @Test
    void generate_withEdgeScores_boundaryValues() {
        // 85 -> 录用
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("基础扎实");
        request.setScore(85);
        assertTrue(algorithm.generate(request).getSuggestion().contains("录用"));

        // 84 -> 下一轮
        request.setScore(84);
        assertTrue(algorithm.generate(request).getSuggestion().contains("下一轮"));

        // 60 -> 下一轮
        request.setScore(60);
        assertTrue(algorithm.generate(request).getSuggestion().contains("下一轮"));

        // 59 -> 不录用
        request.setScore(59);
        String suggestion59 = algorithm.generate(request).getSuggestion();
        assertTrue(suggestion59.contains("不建议录用") || suggestion59.contains("暂不"));
    }

    @Test
    void generate_allAdvantageKeywords_returnsSixItems() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("基础扎实，表达清晰，项目经验丰富，逻辑清晰，学习能力强，态度积极。");

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertEquals(6, response.getAdvantages().size());
    }

    @Test
    void generate_allRiskKeywords_returnsSixItems() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("项目深度不足，经验不足，回答不清晰，基础薄弱，表达较弱，跳槽频繁。");

        FeedbackSummaryResponse response = algorithm.generate(request);

        assertEquals(6, response.getRisks().size());
    }
}

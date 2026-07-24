package com.recruit.ai.algorithm.risk;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnoverRiskAlgorithmTest {

    private TurnoverRiskAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new TurnoverRiskAlgorithm();
    }

    @Test
    void predict_withAllLowScores_returnsHighRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("张三");
        request.setPosition("Java开发");
        request.setPerformanceScore(40);
        request.setAttendanceScore(50);
        request.setSatisfactionScore(30);

        TurnoverRiskResponse response = algorithm.predict(request);

        assertEquals("HIGH", response.getRiskLevel());
        assertTrue(response.getRiskScore() >= 70);
        assertFalse(response.getRiskReasons().isEmpty());
        assertNotNull(response.getSuggestions());
        assertFalse(response.getSuggestions().isEmpty());
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().contains("张三"));
    }

    @Test
    void predict_withMediumScores_returnsMediumRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("李四");
        request.setPosition("产品经理");
        request.setPerformanceScore(59);
        request.setSatisfactionScore(55);

        TurnoverRiskResponse response = algorithm.predict(request);

        assertEquals("MEDIUM", response.getRiskLevel());
        assertTrue(response.getRiskScore() >= 40 && response.getRiskScore() < 70);
    }

    @Test
    void predict_withHighScores_returnsLowRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("王五");
        request.setPosition("前端开发");
        request.setPerformanceScore(90);
        request.setAttendanceScore(95);
        request.setSatisfactionScore(85);

        TurnoverRiskResponse response = algorithm.predict(request);

        assertEquals("LOW", response.getRiskLevel());
        assertTrue(response.getRiskScore() < 40);
    }

    @Test
    void predict_withTurnoverIntentKeywords_addsRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("赵六");
        request.setPosition("测试工程师");
        request.setPerformanceScore(85);
        request.setAttendanceScore(90);
        request.setSatisfactionScore(80);
        request.setLatestFeedback("最近在看新机会，想跳槽去更好的平台");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getRiskReasons().stream().anyMatch(r -> r.contains("离职") || r.contains("跳槽")));
    }

    @Test
    void predict_withStressKeywords_addsRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("孙七");
        request.setPosition("后端开发");
        request.setPerformanceScore(75);
        request.setAttendanceScore(80);
        request.setSatisfactionScore(70);
        request.setLatestFeedback("工作压力大，对薪资不满意，发展空间有限");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getRiskReasons().stream().anyMatch(r -> r.contains("压力") || r.contains("发展诉求")));
        assertTrue(response.getRiskScore() > 0);
    }

    @Test
    void predict_withAttendanceIssues_addsRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("周八");
        request.setPosition("设计师");
        request.setPerformanceScore(80);
        request.setAttendanceScore(55);
        request.setSatisfactionScore(80);
        request.setAttendanceSummary("近期频繁迟到、缺勤");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getRiskReasons().stream().anyMatch(r -> r.contains("考勤")));
    }

    @Test
    void predict_withDecliningTrend_addsRisk() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("吴九");
        request.setPosition("运维");
        request.setPerformanceScore(80);
        request.setAttendanceScore(80);
        request.setSatisfactionScore(80);
        request.setPerformanceTrend("近期绩效下降，积极性下降，状态下降");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getRiskReasons().stream().anyMatch(r -> r.contains("下降趋势")));
    }

    @Test
    void predict_withNullScores_returnsEmptyReasons() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("郑十");
        request.setPosition("HR");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertNotNull(response.getRiskLevel());
        assertEquals(0, response.getRiskScore());
        assertTrue(response.getRiskReasons().isEmpty());
    }

    @Test
    void predict_withEdgeScores_boundaryValues() {
        // 边缘: score 59 触发风险 (<60), score 60 不触发
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setPerformanceScore(59);
        request.setAttendanceScore(59);
        request.setSatisfactionScore(59);
        TurnoverRiskResponse response = algorithm.predict(request);
        assertEquals("HIGH", response.getRiskLevel()); // 25+20+30=75 >= 70

        // 边缘: score 60 不触发任何分数风险
        request = new TurnoverRiskRequest();
        request.setPerformanceScore(60);
        request.setAttendanceScore(60);
        request.setSatisfactionScore(60);
        response = algorithm.predict(request);
        assertEquals(0, response.getRiskScore()); // 都不触发
        assertEquals("LOW", response.getRiskLevel());
    }

    @Test
    void predict_riskScoreCappedAt100() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setPerformanceScore(30);
        request.setAttendanceScore(30);
        request.setSatisfactionScore(30);
        request.setLatestFeedback("想离职跳槽，工作压力大，不满意，经常迟到，绩效下降");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getRiskScore() <= 100);
    }

    @Test
    void predict_sentimentLowScore_returnsPositive() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setSatisfactionScore(90);

        TurnoverRiskResponse response = algorithm.predict(request);

        assertEquals("POSITIVE", response.getSentimentLabel());
        assertTrue(response.getSentimentRiskScore() < 30);
    }

    @Test
    void predict_sentimentMediumScore_returnsNeutral() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setSatisfactionScore(65);

        TurnoverRiskResponse response = algorithm.predict(request);

        assertEquals("NEUTRAL", response.getSentimentLabel());
    }

    @Test
    void predict_sentimentHighScore_returnsNegative() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setSatisfactionScore(30);
        request.setLatestFeedback("想离职跳槽");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertEquals("NEGATIVE", response.getSentimentLabel());
        assertTrue(response.getSentimentRiskScore() >= 60);
    }

    @Test
    void predict_sentimentPositiveWords_reducesScore() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setSatisfactionScore(80);
        request.setLatestFeedback("非常满意现在的工作，团队稳定，喜欢团队氛围");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getSentimentRiskScore() >= 0);
        assertNotNull(response.getSentimentSummary());
    }

    @Test
    void predict_withEmptyEmployeeName_usesDefault() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setPerformanceScore(30);
        request.setSatisfactionScore(30);

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getSummary().contains("该员工"));
    }

    @Test
    void predict_buildSentimentSummary_containsScore() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setSatisfactionScore(30);
        request.setLatestFeedback("想离职");

        TurnoverRiskResponse response = algorithm.predict(request);

        assertTrue(response.getSentimentSummary().contains(String.valueOf(response.getSentimentRiskScore())));
    }
}

package com.recruit.ai.algorithm.match;

import com.recruit.ai.dto.request.ResumeMatchRequest;
import com.recruit.ai.dto.response.ResumeMatchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResumeMatchAlgorithmTest {

    private ResumeMatchAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new ResumeMatchAlgorithm();
    }

    @Test
    void match_withAllFields_returnsHighScore() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("Java后端开发工程师");
        request.setResponsibilities("负责微服务架构设计与接口开发");
        request.setRequirements("精通Java、Spring Boot、MySQL、Redis");
        request.setResumeText("5年后端开发经验，熟练使用Java、Spring Boot");
        request.setSkills("Java, Spring, MySQL, Redis");
        request.setProjectExperience("电商平台微服务项目");
        request.setWorkExperience("某公司后端开发");

        ResumeMatchResponse response = algorithm.match(request);

        assertNotNull(response);
        assertTrue(response.getScore() >= 60);
        assertEquals("HIGH", response.getLevel());
        assertFalse(response.getMatchedPoints().isEmpty());
        assertTrue(response.getSummary().contains("匹配度"));
        assertTrue(response.getSummary().contains("HIGH"));
        assertNotNull(response.getSuggestion());
    }

    @Test
    void match_withBackendKeywords_returnsBackendScore() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("Java开发");
        request.setRequirements("Spring Boot, MySQL, Redis");
        request.setResumeText("精通Java、Spring框架");
        request.setSkills("Java, Spring");

        ResumeMatchResponse response = algorithm.match(request);

        assertTrue(response.getScore() > 0);
        assertFalse(response.getMatchedPoints().isEmpty());
    }

    @Test
    void match_withAIKeywords_returnsAIScore() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("AI算法工程师");
        request.setRequirements("Python, 机器学习, 大模型");
        request.setResumeText("熟练掌握Python、机器学习算法");
        request.setSkills("Python, 机器学习");

        ResumeMatchResponse response = algorithm.match(request);

        assertTrue(response.getScore() > 0);
        assertFalse(response.getMatchedPoints().isEmpty());
    }

    @Test
    void match_withGeneralKeywords_returnsGeneralScore() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("产品经理");
        request.setRequirements("沟通能力强，有项目经验");
        request.setResumeText("具备良好的沟通协作能力和项目经验");
        request.setSkills("沟通, 项目经验");

        ResumeMatchResponse response = algorithm.match(request);

        assertTrue(response.getScore() >= 0);
        assertNotNull(response.getLevel());
    }

    @Test
    void match_withEmptyKeywords_returnsZeroScoreAndLowLevel() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("XXX");
        request.setRequirements("YYY");
        request.setResumeText("ZZZ");
        request.setSkills("AAA");

        ResumeMatchResponse response = algorithm.match(request);

        assertEquals(0, response.getScore());
        assertEquals("LOW", response.getLevel());
        assertTrue(response.getMatchedPoints().isEmpty());
        assertEquals("未能从岗位信息中提取到有效关键词", response.getRiskPoints().get(0));
        assertNotNull(response.getSuggestion());
    }

    @Test
    void match_withNullFields_doesNotThrow() {
        ResumeMatchRequest request = new ResumeMatchRequest();

        ResumeMatchResponse response = algorithm.match(request);

        assertNotNull(response);
        assertNotNull(response.getLevel());
    }

    @Test
    void match_withKnowledgeContext_mergesKeywords() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("Java开发");
        request.setRequirements("Spring Boot");
        request.setResumeText("精通Java");
        request.setSkills("Java");
        String knowledgeContext = "Spring Cloud, MySQL";

        ResumeMatchResponse response = algorithm.match(request, knowledgeContext);

        assertTrue(response.getScore() > 0);
    }

    @Test
    void match_noMatchBetweenJobAndResume_returnsLowLevel() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("AI算法工程师");
        request.setRequirements("Python, 机器学习, 大模型, RAG");
        request.setResumeText("从事前端开发，熟悉HTML、CSS、JavaScript");
        request.setSkills("HTML, CSS");

        ResumeMatchResponse response = algorithm.match(request);

        assertTrue(response.getScore() < 60);
        assertFalse(response.getRiskPoints().isEmpty());
        assertTrue(response.getRiskPoints().stream().anyMatch(r -> r.contains("缺少")));
    }

    @Test
    void match_allCategories_correctlyInferred() {
        // BACKEND
        ResumeMatchRequest backendReq = new ResumeMatchRequest();
        backendReq.setJobTitle("Java后端");
        backendReq.setRequirements("Spring, MySQL");
        backendReq.setResumeText("Java");
        ResumeMatchResponse backendResp = algorithm.match(backendReq);
        assertTrue(backendResp.getScore() > 0);

        // AI
        ResumeMatchRequest aiReq = new ResumeMatchRequest();
        aiReq.setJobTitle("Python算法");
        aiReq.setRequirements("机器学习");
        aiReq.setResumeText("Python");
        ResumeMatchResponse aiResp = algorithm.match(aiReq);
        assertTrue(aiResp.getScore() > 0);

        // GENERAL
        ResumeMatchRequest genReq = new ResumeMatchRequest();
        genReq.setJobTitle("销售");
        genReq.setRequirements("沟通");
        genReq.setResumeText("沟通");
        ResumeMatchResponse genResp = algorithm.match(genReq);
        assertTrue(genResp.getScore() > 0);
    }
}

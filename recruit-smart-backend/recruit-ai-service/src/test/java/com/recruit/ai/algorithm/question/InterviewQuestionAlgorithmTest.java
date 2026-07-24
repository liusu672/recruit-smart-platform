package com.recruit.ai.algorithm.question;

import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.response.InterviewQuestionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterviewQuestionAlgorithmTest {

    private InterviewQuestionAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new InterviewQuestionAlgorithm();
    }

    @Test
    void generate_withJavaBackend_returnsJavaQuestions() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("Java后端开发");
        request.setRequirements("精通Spring Boot、MySQL、Redis、微服务");
        request.setResponsibilities("负责后端接口开发和系统设计");
        request.setResumeText("有多个Java项目开发经验");
        request.setSkills("Java, Spring, MyBatis");
        request.setProjectExperience("电商平台项目");
        request.setWorkExperience("3年后端开发");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertEquals("Java后端", response.getCategory());
        assertFalse(response.getQuestions().isEmpty());
        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("Java")));
        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("接口")));
        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("索引") || q.getTitle().contains("微服务")));
    }

    @Test
    void generate_withAIKeywords_returnsAIQuestions() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("AI算法工程师");
        request.setRequirements("Python, 机器学习, RAG, 向量检索");
        request.setResumeText("做过大模型相关项目");
        request.setSkills("Python, PyTorch");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertEquals("AI算法", response.getCategory());
        assertFalse(response.getQuestions().isEmpty());
        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("AI") || q.getTitle().contains("RAG")));
    }

    @Test
    void generate_withHRKeywords_returnsHRQuestions() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("招聘专员");
        request.setRequirements("熟悉招聘流程、候选人沟通、Offer、入职办理");
        request.setResumeText("负责候选人沟通和入职办理");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertEquals("HR业务", response.getCategory());
        assertFalse(response.getQuestions().isEmpty());
        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("招聘") || q.getTitle().contains("Offer")));
    }

    @Test
    void generate_withGeneralKeywords_returnsGeneralQuestions() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("行政专员");
        request.setRequirements("沟通能力强");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertFalse(response.getQuestions().isEmpty());
        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("核心项目")));
    }

    @Test
    void generate_resumeContainsProject_addsProjectFollowUp() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("Java开发");
        request.setRequirements("Java");
        request.setResumeText("我做过很多项目");
        request.setSkills("Java");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("项目追问")));
    }

    @Test
    void generate_resumeContainsResponsibility_addsResponsibilityFollowUp() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("Java开发");
        request.setRequirements("Java");
        request.setResumeText("我负责过核心模块");
        request.setSkills("Java");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertTrue(response.getQuestions().stream().anyMatch(q -> q.getTitle().contains("职责追问")));
    }

    @Test
    void generate_eachQuestionHasRequiredFields() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("Java开发");
        request.setRequirements("Spring Boot");

        InterviewQuestionResponse response = algorithm.generate(request);

        assertFalse(response.getQuestions().isEmpty());
        response.getQuestions().forEach(q -> {
            assertNotNull(q.getTitle());
            assertNotNull(q.getContent());
            assertNotNull(q.getFocus());
            assertFalse(q.getFocus().isEmpty());
            assertNotNull(q.getDifficulty());
            assertNotNull(q.getAnswerPoints());
        });
    }

    @Test
    void generate_withNullFields_doesNotThrow() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();

        InterviewQuestionResponse response = algorithm.generate(request);

        assertNotNull(response);
        assertFalse(response.getQuestions().isEmpty());
    }
}

package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.FeedbackSummaryRequest;
import com.recruit.ai.dto.request.InterviewQuestionRequest;
import com.recruit.ai.dto.request.ResumeMatchRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtherPromptsTest {

    // ========== ResumeMatchPrompts ==========

    @Test
    void resumeMatchPrompts_versionAndSystemPrompt() {
        assertNotNull(ResumeMatchPrompts.VERSION);
        assertFalse(ResumeMatchPrompts.SYSTEM_PROMPT.isBlank());
        assertTrue(ResumeMatchPrompts.SYSTEM_PROMPT.contains("简历匹配"));
    }

    @Test
    void resumeMatchPrompts_buildUserPrompt_containsAllFields() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        request.setJobTitle("Java开发");
        request.setRequirements("Spring Boot");
        request.setResumeText("5年Java经验");
        request.setSkills("Java, Spring");
        request.setProjectExperience("电商平台");
        request.setWorkExperience("某公司");

        String prompt = ResumeMatchPrompts.buildUserPrompt(request, "知识库参考");
        assertTrue(prompt.contains("Java开发"));
        assertTrue(prompt.contains("Spring Boot"));
        assertTrue(prompt.contains("5年Java经验"));
        assertTrue(prompt.contains("知识库参考"));
    }

    @Test
    void resumeMatchPrompts_buildUserPrompt_withNulls_usesEmpty() {
        ResumeMatchRequest request = new ResumeMatchRequest();
        String prompt = ResumeMatchPrompts.buildUserPrompt(request, null);
        assertNotNull(prompt);
        assertFalse(prompt.contains("null"));
    }

    // ========== InterviewQuestionPrompts ==========

    @Test
    void interviewQuestionPrompts_versionAndSystemPrompt() {
        assertNotNull(InterviewQuestionPrompts.VERSION);
        assertFalse(InterviewQuestionPrompts.SYSTEM_PROMPT.isBlank());
        assertTrue(InterviewQuestionPrompts.SYSTEM_PROMPT.contains("面试题生成"));
    }

    @Test
    void interviewQuestionPrompts_buildUserPrompt_containsAllFields() {
        InterviewQuestionRequest request = new InterviewQuestionRequest();
        request.setJobTitle("Java后端");
        request.setRequirements("Spring Boot, MySQL");
        request.setResumeText("5年Java开发经验");
        request.setSkills("Java, Spring");
        request.setProjectExperience("电商项目");
        request.setWorkExperience("某科技公司");

        String prompt = InterviewQuestionPrompts.buildUserPrompt(request, "面试题参考");
        assertTrue(prompt.contains("Java后端"));
        assertTrue(prompt.contains("Spring Boot"));
        assertTrue(prompt.contains("5年Java开发经验"));
        assertTrue(prompt.contains("面试题参考"));
    }

    @Test
    void interviewQuestionPrompts_buildUserPrompt_withNulls_usesEmpty() {
        String prompt = InterviewQuestionPrompts.buildUserPrompt(new InterviewQuestionRequest(), null);
        assertNotNull(prompt);
        assertFalse(prompt.contains("null"));
    }

    // ========== FeedbackSummaryPrompts ==========

    @Test
    void feedbackSummaryPrompts_versionAndSystemPrompt() {
        assertNotNull(FeedbackSummaryPrompts.VERSION);
        assertFalse(FeedbackSummaryPrompts.SYSTEM_PROMPT.isBlank());
        assertTrue(FeedbackSummaryPrompts.SYSTEM_PROMPT.contains("面试反馈摘要"));
    }

    @Test
    void feedbackSummaryPrompts_buildUserPrompt_containsAllFields() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setJobTitle("Java开发");
        request.setCandidateName("张三");
        request.setScore(85);
        request.setFeedbackText("基础扎实，表达清晰");

        String prompt = FeedbackSummaryPrompts.buildUserPrompt(request, "评价标准参考");
        assertTrue(prompt.contains("Java开发"));
        assertTrue(prompt.contains("张三"));
        assertTrue(prompt.contains("85"));
        assertTrue(prompt.contains("基础扎实，表达清晰"));
        assertTrue(prompt.contains("评价标准参考"));
    }

    @Test
    void feedbackSummaryPrompts_buildUserPrompt_withNullScore_showsNotProvided() {
        FeedbackSummaryRequest request = new FeedbackSummaryRequest();
        request.setFeedbackText("表现良好");
        String prompt = FeedbackSummaryPrompts.buildUserPrompt(request, "");
        assertTrue(prompt.contains("未提供"));
    }

    @Test
    void feedbackSummaryPrompts_buildUserPrompt_withNulls_usesEmpty() {
        String prompt = FeedbackSummaryPrompts.buildUserPrompt(new FeedbackSummaryRequest(), null);
        assertNotNull(prompt);
        assertFalse(prompt.contains("null"));
    }

    // ========== ResumeParsePrompts ==========

    @Test
    void resumeParsePrompts_versionAndSystemPrompt() {
        assertNotNull(ResumeParsePrompts.VERSION);
        assertFalse(ResumeParsePrompts.SYSTEM_PROMPT.isBlank());
        assertTrue(ResumeParsePrompts.SYSTEM_PROMPT.contains("简历信息抽取"));
    }

    @Test
    void resumeParsePrompts_buildUserPrompt_containsResumeText() {
        String prompt = ResumeParsePrompts.buildUserPrompt("张三，5年Java开发经验");
        assertTrue(prompt.contains("张三"));
        assertTrue(prompt.contains("5年Java开发经验"));
    }

    @Test
    void resumeParsePrompts_buildUserPrompt_withNull_usesEmpty() {
        String prompt = ResumeParsePrompts.buildUserPrompt(null);
        assertNotNull(prompt);
        assertFalse(prompt.contains("null"));
    }

    // ========== ToolChatPrompts ==========

    @Test
    void toolChatPrompts_systemPrompt_isNotEmpty() {
        assertNotNull(ToolChatPrompts.SYSTEM_PROMPT);
        assertFalse(ToolChatPrompts.SYSTEM_PROMPT.isBlank());
        assertTrue(ToolChatPrompts.SYSTEM_PROMPT.contains("工具"));
        assertTrue(ToolChatPrompts.SYSTEM_PROMPT.contains("规则"));
    }
}

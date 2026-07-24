package com.recruit.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.feign.dto.request.*;
import com.recruit.feign.dto.response.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 feign-api 模块中所有 DTO 的 JSON 序列化/反序列化。
 */
class FeignDtoSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    void serializeResumeMatchRequest() throws Exception {
        ResumeMatchRequest dto = new ResumeMatchRequest();
        dto.setApplicationId(100L);
        dto.setJobId(200L);
        dto.setJobTitle("Java开发");

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"applicationId\":100"));
        assertTrue(json.contains("\"jobTitle\":\"Java开发\""));

        ResumeMatchRequest deserialized = mapper.readValue(json, ResumeMatchRequest.class);
        assertEquals(100L, deserialized.getApplicationId());
        assertEquals("Java开发", deserialized.getJobTitle());
    }

    @Test
    void serializeResumeMatchResponse() throws Exception {
        ResumeMatchResponse dto = new ResumeMatchResponse();
        dto.setScore(85);
        dto.setLevel("HIGH");
        dto.setMatchedPoints(List.of("Java", "Spring"));

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"score\":85"));
        assertTrue(json.contains("\"level\":\"HIGH\""));

        ResumeMatchResponse deserialized = mapper.readValue(json, ResumeMatchResponse.class);
        assertEquals(85, deserialized.getScore());
        assertEquals(List.of("Java", "Spring"), deserialized.getMatchedPoints());
    }

    @Test
    void serializeResumeParseResponse() throws Exception {
        ResumeParseResponse dto = new ResumeParseResponse();
        dto.setParsedContent("简历文本");
        dto.setSkills(List.of("Java", "Spring"));
        dto.setWarnings(List.of("信息不完整"));

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"parsedContent\":\"简历文本\""));

        ResumeParseResponse deserialized = mapper.readValue(json, ResumeParseResponse.class);
        assertEquals("简历文本", deserialized.getParsedContent());
        assertEquals(List.of("Java", "Spring"), deserialized.getSkills());
    }

    @Test
    void serializeInterviewQuestionRequest() throws Exception {
        InterviewQuestionRequest dto = new InterviewQuestionRequest();
        dto.setInterviewId(10L);
        dto.setJobTitle("Java开发");

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"interviewId\":10"));

        InterviewQuestionRequest deserialized = mapper.readValue(json, InterviewQuestionRequest.class);
        assertEquals(10L, deserialized.getInterviewId());
    }

    @Test
    void serializeInterviewQuestionResponse() throws Exception {
        InterviewQuestionResponse dto = new InterviewQuestionResponse();
        dto.setCategory("Java后端");
        dto.setSummary("已生成");

        InterviewQuestionItemResponse item = new InterviewQuestionItemResponse();
        item.setTitle("Java基础");
        item.setContent("请介绍Java");
        item.setFocus(List.of("基本功"));
        item.setDifficulty("MEDIUM");
        dto.setQuestions(List.of(item));

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"category\":\"Java后端\""));
        assertTrue(json.contains("\"title\":\"Java基础\""));

        InterviewQuestionResponse deserialized = mapper.readValue(json, InterviewQuestionResponse.class);
        assertEquals("Java后端", deserialized.getCategory());
        assertEquals(1, deserialized.getQuestions().size());
        assertEquals("Java基础", deserialized.getQuestions().get(0).getTitle());
    }

    @Test
    void serializeFeedbackSummaryRequest() throws Exception {
        FeedbackSummaryRequest dto = new FeedbackSummaryRequest();
        dto.setCandidateName("张三");
        dto.setFeedbackText("表现良好");
        dto.setScore(85);

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"candidateName\":\"张三\""));

        FeedbackSummaryRequest deserialized = mapper.readValue(json, FeedbackSummaryRequest.class);
        assertEquals("张三", deserialized.getCandidateName());
        assertEquals(85, deserialized.getScore());
    }

    @Test
    void serializeFeedbackSummaryResponse() throws Exception {
        FeedbackSummaryResponse dto = new FeedbackSummaryResponse();
        dto.setSummary("优秀候选人");
        dto.setAdvantages(List.of("基础扎实"));
        dto.setRisks(List.of("项目经验不足"));
        dto.setSuggestion("建议录用");

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"suggestion\":\"建议录用\""));

        FeedbackSummaryResponse deserialized = mapper.readValue(json, FeedbackSummaryResponse.class);
        assertEquals("建议录用", deserialized.getSuggestion());
    }

    @Test
    void serializeTurnoverRiskRequest() throws Exception {
        TurnoverRiskRequest dto = new TurnoverRiskRequest();
        dto.setEmployeeId(1001L);
        dto.setEmployeeName("张三");
        dto.setPerformanceScore(40);
        dto.setSatisfactionScore(30);
        dto.setPerformanceTrend("持续下降");
        dto.setLatestFeedback("想离职");

        EmployeeBehaviorRecordDTO record = new EmployeeBehaviorRecordDTO();
        record.setRecordId(1L);
        record.setPerformanceScore(50);
        record.setLateCount(3);
        record.setFeedbackText("工作压力大");
        dto.setBehaviorRecords(List.of(record));

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"employeeName\":\"张三\""));
        assertTrue(json.contains("\"performanceTrend\":\"持续下降\""));

        TurnoverRiskRequest deserialized = mapper.readValue(json, TurnoverRiskRequest.class);
        assertEquals(1001L, deserialized.getEmployeeId());
        assertEquals("张三", deserialized.getEmployeeName());
        assertEquals(1, deserialized.getBehaviorRecords().size());
    }

    @Test
    void serializeTurnoverRiskResponse() throws Exception {
        TurnoverRiskResponse dto = new TurnoverRiskResponse();
        dto.setSentimentLabel("NEGATIVE");
        dto.setSentimentRiskScore(80);
        dto.setRiskLevel("HIGH");
        dto.setRiskScore(75);
        dto.setSummary("高风险");
        dto.setRiskReasons(List.of("绩效偏低", "满意度低"));
        dto.setSuggestions(List.of("建议一对一沟通"));

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"riskLevel\":\"HIGH\""));

        TurnoverRiskResponse deserialized = mapper.readValue(json, TurnoverRiskResponse.class);
        assertEquals("HIGH", deserialized.getRiskLevel());
        assertEquals(2, deserialized.getRiskReasons().size());
    }

    @Test
    void serializeTurnoverRiskHistoryResponse() throws Exception {
        TurnoverRiskHistoryResponse dto = new TurnoverRiskHistoryResponse();
        dto.setId(1L);
        dto.setTaskId(10L);
        dto.setEmployeeId(1001L);
        dto.setRiskLevel("LOW");
        dto.setRiskScore(20);
        dto.setSource("LLM");
        dto.setGeneratedAt(LocalDateTime.now());
        dto.setRiskReasons(List.of("暂无"));
        dto.setBehaviorRecordIds(List.of(1L, 2L));

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"riskLevel\":\"LOW\""));

        TurnoverRiskHistoryResponse deserialized = mapper.readValue(json, TurnoverRiskHistoryResponse.class);
        assertEquals("LOW", deserialized.getRiskLevel());
        assertEquals(List.of(1L, 2L), deserialized.getBehaviorRecordIds());
    }

    @Test
    void serializeEmployeeBehaviorRecordDTO() throws Exception {
        EmployeeBehaviorRecordDTO dto = new EmployeeBehaviorRecordDTO();
        dto.setRecordId(1L);
        dto.setPeriodStart(LocalDate.of(2026, 1, 1));
        dto.setPerformanceScore(80);
        dto.setTaskCompletionRate(BigDecimal.valueOf(0.95));
        dto.setLateCount(2);
        dto.setAbsenceDays(BigDecimal.ZERO);
        dto.setFeedbackText("表现稳定");

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"recordId\":1"));

        EmployeeBehaviorRecordDTO deserialized = mapper.readValue(json, EmployeeBehaviorRecordDTO.class);
        assertEquals(1L, deserialized.getRecordId());
        assertEquals(80, deserialized.getPerformanceScore());
    }

    @Test
    void serializeDtoWithNullFields_doesNotThrow() {
        assertDoesNotThrow(() -> mapper.writeValueAsString(new ResumeMatchRequest()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new ResumeMatchResponse()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new InterviewQuestionRequest()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new InterviewQuestionResponse()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new FeedbackSummaryRequest()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new FeedbackSummaryResponse()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new TurnoverRiskRequest()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new TurnoverRiskResponse()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new ResumeParseResponse()));
        assertDoesNotThrow(() -> mapper.writeValueAsString(new EmployeeBehaviorRecordDTO()));
    }
}

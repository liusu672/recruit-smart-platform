package com.recruit.ai.prompt;

import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.feign.dto.request.EmployeeBehaviorRecordDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnoverRiskPromptsTest {

    @Test
    void version_isNotEmpty() {
        assertNotNull(TurnoverRiskPrompts.VERSION);
        assertFalse(TurnoverRiskPrompts.VERSION.isBlank());
    }

    @Test
    void systemPrompt_isNotEmpty() {
        assertNotNull(TurnoverRiskPrompts.SYSTEM_PROMPT);
        assertFalse(TurnoverRiskPrompts.SYSTEM_PROMPT.isBlank());
        assertTrue(TurnoverRiskPrompts.SYSTEM_PROMPT.contains("风险分析助手"));
        assertTrue(TurnoverRiskPrompts.SYSTEM_PROMPT.contains("sentimentLabel"));
        assertTrue(TurnoverRiskPrompts.SYSTEM_PROMPT.contains("riskLevel"));
    }

    @Test
    void buildUserPrompt_containsAllSections() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeId(1001L);
        request.setEmployeeName("张三");
        request.setDepartment("技术部");
        request.setPosition("Java开发");
        request.setPerformanceTrend("持续下降");
        request.setPerformanceScore(40);
        request.setAttendanceScore(50);
        request.setSatisfactionScore(30);
        request.setLatestFeedback("最近工作压力大");

        String prompt = TurnoverRiskPrompts.buildUserPrompt(request, "知识库参考内容");

        assertTrue(prompt.contains("张三"));
        assertTrue(prompt.contains("技术部"));
        assertTrue(prompt.contains("Java开发"));
        assertTrue(prompt.contains("持续下降"));
        assertTrue(prompt.contains("最近工作压力大"));
        assertTrue(prompt.contains("知识库参考内容"));
        assertTrue(prompt.contains("【员工基础信息】"));
        assertTrue(prompt.contains("【多周期趋势】"));
        assertTrue(prompt.contains("【最近一期数据】"));
        assertTrue(prompt.contains("【各周期行为数据】"));
        assertTrue(prompt.contains("【知识库参考内容】"));
    }

    @Test
    void buildUserPrompt_withBehaviorRecords_includesRecords() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeName("李四");

        EmployeeBehaviorRecordDTO record = new EmployeeBehaviorRecordDTO();
        record.setPeriodStart(LocalDate.of(2026, 1, 1));
        record.setPeriodEnd(LocalDate.of(2026, 3, 31));
        record.setPerformanceScore(70);
        record.setLateCount(2);
        record.setFeedbackText("工作状态一般");
        request.setBehaviorRecords(List.of(record));

        String prompt = TurnoverRiskPrompts.buildUserPrompt(request, "");

        assertTrue(prompt.contains("2026-01-01"));
        assertTrue(prompt.contains("2026-03-31"));
        assertTrue(prompt.contains("工作状态一般"));
    }

    @Test
    void buildUserPrompt_withNullFields_doesNotThrow() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        assertDoesNotThrow(() -> TurnoverRiskPrompts.buildUserPrompt(request, null));
    }

    @Test
    void buildUserPrompt_withEmptyBehaviorRecords_showsNoDataMessage() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setBehaviorRecords(List.of());
        String prompt = TurnoverRiskPrompts.buildUserPrompt(request, "");
        assertTrue(prompt.contains("没有提供多周期行为数据"));
    }
}

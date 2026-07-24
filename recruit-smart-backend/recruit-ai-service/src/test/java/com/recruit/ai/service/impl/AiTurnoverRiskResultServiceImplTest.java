package com.recruit.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.ai.dto.request.TurnoverRiskRequest;
import com.recruit.ai.dto.response.TurnoverRiskResponse;
import com.recruit.ai.entity.AiTurnoverRiskResult;
import com.recruit.ai.mapper.AiTurnoverRiskResultMapper;
import com.recruit.feign.dto.request.EmployeeBehaviorRecordDTO;
import com.recruit.feign.dto.response.TurnoverRiskHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTurnoverRiskResultServiceImplTest {

    @Mock
    private AiTurnoverRiskResultMapper mapper;
    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<AiTurnoverRiskResult> resultCaptor;

    private AiTurnoverRiskResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiTurnoverRiskResultServiceImpl(mapper, objectMapper);
    }

    @Test
    void saveResult_withFullData_insertsSuccessfully() throws Exception {
        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse response = createResponse();

        when(objectMapper.writeValueAsString(any())).thenReturn("[\"风险原因\"]");
        when(mapper.insert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "LLM");

        verify(mapper).insert(resultCaptor.capture());
        AiTurnoverRiskResult result = resultCaptor.getValue();

        assertEquals(1L, result.getTaskId());
        assertEquals(1001L, result.getEmployeeId());
        assertEquals("HIGH", result.getRiskLevel());
        assertEquals(75, result.getRiskScore());
        assertEquals("NEGATIVE", result.getSentimentLabel());
        assertEquals("[\"风险原因\"]", result.getRiskReasons());
        assertEquals("[\"风险原因\"]", result.getSuggestions());
        assertEquals("LLM", result.getSource());
        assertNotNull(result.getGeneratedAt());
    }

    @Test
    void saveResult_withNullLists_serializesEmptyArray() throws Exception {
        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse response = createResponse();
        response.setRiskReasons(null);
        response.setSuggestions(null);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("[]")  // riskReasons
                .thenReturn("[]")  // suggestions
                .thenReturn("[]"); // behaviorRecordIds
        when(mapper.insert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "RULE");

        verify(mapper).insert(resultCaptor.capture());
        assertEquals("[]", resultCaptor.getValue().getRiskReasons());
        assertEquals("[]", resultCaptor.getValue().getSuggestions());
    }

    @Test
    void saveResult_withBehaviorRecords_extractsIds() throws Exception {
        TurnoverRiskRequest request = createRequest();
        List<EmployeeBehaviorRecordDTO> records = List.of(
                createRecord(10L),
                createRecord(20L)
        );
        request.setBehaviorRecords(records);

        TurnoverRiskResponse response = createResponse();

        when(objectMapper.writeValueAsString(any())).thenReturn("[\"风险\"]");
        when(objectMapper.writeValueAsString(List.of(10L, 20L))).thenReturn("[10,20]");
        when(mapper.insert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "LLM");

        verify(mapper).insert(resultCaptor.capture());
        assertEquals("[10,20]", resultCaptor.getValue().getBehaviorRecordIds());
    }

    @Test
    void saveResult_whenInsertFails_throwsException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mapper.insert(any())).thenReturn(0);

        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse response = createResponse();

        assertThrows(IllegalStateException.class,
                () -> service.saveResult(1L, request, response, "LLM"));
    }

    @Test
    void saveResult_whenMapperThrows_wrapsException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mapper.insert(any())).thenThrow(new RuntimeException("数据库异常"));

        TurnoverRiskRequest request = createRequest();
        TurnoverRiskResponse response = createResponse();

        assertThrows(IllegalStateException.class,
                () -> service.saveResult(1L, request, response, "LLM"));
    }

    @Test
    void listByEmployeeId_withResults_returnsHistory() throws Exception {
        AiTurnoverRiskResult entity = new AiTurnoverRiskResult();
        entity.setId(1L);
        entity.setTaskId(10L);
        entity.setEmployeeId(1001L);
        entity.setRiskLevel("LOW");
        entity.setRiskScore(20);
        entity.setRiskReasons("[\"原因1\"]");
        entity.setSuggestions("[\"建议1\"]");
        entity.setBehaviorRecordIds("[]");
        entity.setSource("LLM");

        when(mapper.selectList(any())).thenReturn(List.of(entity));
        when(objectMapper.readValue(eq("[\"原因1\"]"), any(TypeReference.class)))
                .thenReturn(List.of("原因1"));
        when(objectMapper.readValue(eq("[\"建议1\"]"), any(TypeReference.class)))
                .thenReturn(List.of("建议1"));
        when(objectMapper.readValue(eq("[]"), any(TypeReference.class)))
                .thenReturn(List.of());

        List<TurnoverRiskHistoryResponse> history = service.listByEmployeeId(1001L);

        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals("LOW", history.get(0).getRiskLevel());
        assertEquals(1L, history.get(0).getId());
    }

    @Test
    void listByEmployeeId_withNullEmployeeId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listByEmployeeId(null));
    }

    @Test
    void listByEmployeeId_withNoResults_returnsEmptyList() {
        when(mapper.selectList(any())).thenReturn(List.of());

        List<TurnoverRiskHistoryResponse> history = service.listByEmployeeId(999L);

        assertTrue(history.isEmpty());
    }

    @Test
    void saveResult_withPeriod_setsPeriodFields() throws Exception {
        TurnoverRiskRequest request = createRequest();
        request.setPeriodStart(LocalDate.of(2026, 1, 1));
        request.setPeriodEnd(LocalDate.of(2026, 6, 30));

        TurnoverRiskResponse response = createResponse();

        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mapper.insert(any())).thenReturn(1);

        service.saveResult(1L, request, response, "LLM");

        verify(mapper).insert(resultCaptor.capture());
        assertEquals(LocalDate.of(2026, 1, 1), resultCaptor.getValue().getPeriodStart());
        assertEquals(LocalDate.of(2026, 6, 30), resultCaptor.getValue().getPeriodEnd());
    }

    private static TurnoverRiskRequest createRequest() {
        TurnoverRiskRequest request = new TurnoverRiskRequest();
        request.setEmployeeId(1001L);
        return request;
    }

    private static TurnoverRiskResponse createResponse() {
        TurnoverRiskResponse response = new TurnoverRiskResponse();
        response.setSentimentLabel("NEGATIVE");
        response.setSentimentRiskScore(80);
        response.setSentimentSummary("负面情绪");
        response.setRiskLevel("HIGH");
        response.setRiskScore(75);
        response.setSummary("高风险");
        response.setRiskReasons(List.of("绩效偏低", "满意度低"));
        response.setSuggestions(List.of("建议沟通"));
        return response;
    }

    private static EmployeeBehaviorRecordDTO createRecord(Long id) {
        EmployeeBehaviorRecordDTO record = new EmployeeBehaviorRecordDTO();
        record.setRecordId(id);
        return record;
    }
}

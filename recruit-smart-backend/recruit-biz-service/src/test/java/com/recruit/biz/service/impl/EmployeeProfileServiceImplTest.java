package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.recruit.biz.dto.EmployeeStatusUpdateDTO;
import com.recruit.biz.dto.EmployeeQueryDTO;
import com.recruit.biz.dto.EmployeeRiskDataUpdateDTO;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.EmployeeDetailVO;
import com.recruit.biz.vo.EmployeeSummaryVO;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeProfileServiceImplTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                EmployeeProfile.class
        );
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                Candidate.class
        );
    }

    @Mock private EmployeeProfileMapper employeeProfileMapper;
    @Mock private CandidateMapper candidateMapper;

    @InjectMocks
    private EmployeeProfileServiceImpl employeeProfileService;

    @BeforeEach
    void setUp() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }

    @AfterEach
    void clear() { UserContext.clear(); }

    @Test
    void listEmployeesReturnsPagedResults() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setName("张三"); emp.setEmployeeNo("EMP20260720000001");
        emp.setStatus("ACTIVE"); emp.setDepartment("研发部");

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EmployeeProfile> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 1);
        page.setRecords(List.of(emp));
        when(employeeProfileMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<EmployeeSummaryVO> result = employeeProfileService.listEmployees(new EmployeeQueryDTO());
        assertEquals(1L, result.getTotal());
        assertEquals("张三", result.getRecords().get(0).getName());
    }

    @Test
    void getDetailSuccess() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setName("张三"); emp.setEmployeeNo("EMP001");
        emp.setStatus("ACTIVE"); emp.setDepartment("研发部");
        emp.setPosition("Java开发");
        when(employeeProfileMapper.selectById(1L)).thenReturn(emp);

        EmployeeDetailVO detail = employeeProfileService.getDetail(1L);
        assertNotNull(detail);
        assertEquals("张三", detail.getName());
        assertEquals("ACTIVE", detail.getStatus());
    }

    @Test
    void getDetailNotFoundThrows() {
        when(employeeProfileMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> employeeProfileService.getDetail(99L));
    }

    @Test
    void updateStatusProbationToActiveSuccess() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setStatus("PROBATION"); emp.setCandidateId(5L);
        when(employeeProfileMapper.selectById(1L)).thenReturn(emp);
        when(employeeProfileMapper.update(eq(null), any())).thenReturn(1);

        EmployeeStatusUpdateDTO dto = new EmployeeStatusUpdateDTO();
        dto.setStatus("ACTIVE");
        employeeProfileService.updateStatus(1L, dto);

        verify(employeeProfileMapper).update(eq(null), any());
    }

    @Test
    void updateStatusInvalidTransitionThrows() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setStatus("LEFT");
        when(employeeProfileMapper.selectById(1L)).thenReturn(emp);

        EmployeeStatusUpdateDTO dto = new EmployeeStatusUpdateDTO();
        dto.setStatus("ACTIVE");
        assertThrows(BusinessException.class,
                () -> employeeProfileService.updateStatus(1L, dto));
    }

    @Test
    void updateStatusActiveToLeftUpdatesCandidate() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setStatus("PROBATION"); emp.setCandidateId(5L);
        when(employeeProfileMapper.selectById(1L)).thenReturn(emp);
        when(employeeProfileMapper.update(eq(null), any())).thenReturn(1);
        Candidate candidate = new Candidate(); candidate.setId(5L);
        when(candidateMapper.selectById(5L)).thenReturn(candidate);
        when(candidateMapper.update(eq(null), any())).thenReturn(1);

        EmployeeStatusUpdateDTO dto = new EmployeeStatusUpdateDTO();
        dto.setStatus("LEFT");
        employeeProfileService.updateStatus(1L, dto);

        verify(candidateMapper).update(eq(null), any());
    }

    @Test
    void updateRiskDataSuccess() {
        EmployeeProfile employee = new EmployeeProfile();
        employee.setId(1L);
        when(employeeProfileMapper.selectById(1L)).thenReturn(employee);
        when(employeeProfileMapper.update(eq(null), any())).thenReturn(1);

        EmployeeRiskDataUpdateDTO dto = new EmployeeRiskDataUpdateDTO();
        dto.setPerformanceSummary("本期任务按计划完成");
        dto.setPerformanceScore(82);
        dto.setAttendanceSummary("无迟到和缺勤记录");
        dto.setAttendanceScore(100);
        dto.setSatisfactionFeedback("对当前工作安排基本满意");
        dto.setSatisfactionScore(85);

        employeeProfileService.updateRiskData(1L, dto);

        verify(employeeProfileMapper).update(eq(null), any());
    }
}

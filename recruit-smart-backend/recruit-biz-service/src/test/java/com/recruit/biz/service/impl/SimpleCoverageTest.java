package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.EmployeeQueryDTO;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.security.CurrentUser;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.vo.EmployeeSummaryVO;
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
class SimpleCoverageTest {

    @BeforeAll
    static void init() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), EmployeeProfile.class);
    }

    @Mock private EmployeeProfileMapper employeeProfileMapper;
    @Mock private CandidateMapper candidateMapper;
    @InjectMocks private EmployeeProfileServiceImpl empService;

    @BeforeEach void setUp() { UserContext.set(new CurrentUser(1L, "hr", "HR")); }
    @AfterEach void clear() { UserContext.clear(); }

    @Test
    void listEmployeesWithFilters() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setName("张三"); emp.setEmployeeNo("EMP001");
        emp.setStatus("ACTIVE"); emp.setDepartment("研发部");

        Page<EmployeeProfile> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(emp));
        when(employeeProfileMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        dto.setKeyword("张三");
        dto.setDepartment("研发部");
        dto.setStatus("ACTIVE");

        PageResult<EmployeeSummaryVO> result = empService.listEmployees(dto);
        assertEquals(1L, result.getTotal());
        assertEquals("张三", result.getRecords().get(0).getName());
    }

    @Test
    void listEmployeesKeywordOnly() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(1L); emp.setName("李四"); emp.setEmployeeNo("EMP002");
        emp.setStatus("PROBATION");

        Page<EmployeeProfile> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(emp));
        when(employeeProfileMapper.selectPage(any(), any())).thenReturn(page);

        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        dto.setKeyword("李四");

        PageResult<EmployeeSummaryVO> result = empService.listEmployees(dto);
        assertEquals(1, result.getRecords().size());
    }
}

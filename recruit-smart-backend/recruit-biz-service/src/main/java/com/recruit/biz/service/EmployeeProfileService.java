package com.recruit.biz.service;

import com.recruit.biz.dto.EmployeeQueryDTO;
import com.recruit.biz.dto.EmployeeStatusUpdateDTO;
import com.recruit.biz.vo.EmployeeDetailVO;
import com.recruit.biz.vo.EmployeeSummaryVO;
import com.recruit.common.result.PageResult;

public interface EmployeeProfileService {
    PageResult<EmployeeSummaryVO> listEmployees(EmployeeQueryDTO dto);
    EmployeeDetailVO getDetail(Long id);
    void updateStatus(Long id, EmployeeStatusUpdateDTO dto);
}

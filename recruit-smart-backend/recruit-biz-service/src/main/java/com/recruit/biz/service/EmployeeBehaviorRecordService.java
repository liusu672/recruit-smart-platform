package com.recruit.biz.service;

import com.recruit.biz.dto.EmployeeBehaviorSaveDTO;
import com.recruit.biz.vo.EmployeeBehaviorRecordVO;

import java.util.List;

public interface EmployeeBehaviorRecordService {

    Long create(
            Long employeeId,
            EmployeeBehaviorSaveDTO dto
    );

    List<EmployeeBehaviorRecordVO> list(
            Long employeeId
    );

    void update(
            Long employeeId,
            Long recordId,
            EmployeeBehaviorSaveDTO dto
    );

    void confirm(
            Long employeeId,
            Long recordId
    );
}
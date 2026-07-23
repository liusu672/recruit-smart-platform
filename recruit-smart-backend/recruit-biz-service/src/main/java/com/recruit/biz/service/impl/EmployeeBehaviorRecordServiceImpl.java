package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.biz.dto.EmployeeBehaviorSaveDTO;
import com.recruit.biz.entity.EmployeeBehaviorRecord;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.enums.BehaviorRecordStatus;
import com.recruit.biz.enums.BehaviorSourceType;
import com.recruit.biz.mapper.EmployeeBehaviorRecordMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.EmployeeBehaviorRecordService;
import com.recruit.biz.vo.EmployeeBehaviorRecordVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeBehaviorRecordServiceImpl
        implements EmployeeBehaviorRecordService {

    private static final Set<String> STAFF_ROLES =
            Set.of("HR", "ADMIN");

    private final EmployeeBehaviorRecordMapper behaviorMapper;
    private final EmployeeProfileMapper employeeMapper;

    @Override
    @Transactional
    public Long create(
            Long employeeId,
            EmployeeBehaviorSaveDTO dto
    ) {
        requireStaffRole();
        requireEmployee(employeeId);
        validatePeriod(employeeId, null, dto);

        EmployeeBehaviorRecord record =
                new EmployeeBehaviorRecord();

        record.setEmployeeId(employeeId);
        applyDTO(dto, record);

        record.setRecordStatus(
                BehaviorRecordStatus.DRAFT.name()
        );
        record.setCreatedBy(UserContext.getUserId());

        int inserted = behaviorMapper.insert(record);

        if (inserted != 1 || record.getId() == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "新增员工行为记录失败"
            );
        }

        return record.getId();
    }

    @Override
    public List<EmployeeBehaviorRecordVO> list(
            Long employeeId
    ) {
        requireStaffRole();
        requireEmployee(employeeId);

        List<EmployeeBehaviorRecord> records =
                behaviorMapper.selectList(
                        new LambdaQueryWrapper
                                <EmployeeBehaviorRecord>()
                                .eq(
                                        EmployeeBehaviorRecord
                                                ::getEmployeeId,
                                        employeeId
                                )
                                .orderByDesc(
                                        EmployeeBehaviorRecord
                                                ::getPeriodStart
                                )
                                .orderByDesc(
                                        EmployeeBehaviorRecord
                                                ::getId
                                )
                );

        return records.stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    public void update(
            Long employeeId,
            Long recordId,
            EmployeeBehaviorSaveDTO dto
    ) {
        requireStaffRole();

        EmployeeBehaviorRecord record =
                requireRecord(employeeId, recordId);

        if (!BehaviorRecordStatus.DRAFT.name()
                .equals(record.getRecordStatus())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "已确认的行为记录不能修改"
            );
        }

        validatePeriod(employeeId, recordId, dto);
        applyDTO(dto, record);

        int updated = behaviorMapper.updateById(record);

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "修改员工行为记录失败"
            );
        }
    }

    @Override
    @Transactional
    public void confirm(
            Long employeeId,
            Long recordId
    ) {
        requireStaffRole();

        EmployeeBehaviorRecord record =
                requireRecord(employeeId, recordId);

        if (!BehaviorRecordStatus.DRAFT.name()
                .equals(record.getRecordStatus())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "行为记录不是草稿状态"
            );
        }

        validateConfirmable(record);

        record.setRecordStatus(
                BehaviorRecordStatus.CONFIRMED.name()
        );

        int updated = behaviorMapper.updateById(record);

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "确认员工行为记录失败"
            );
        }
    }

    private void applyDTO(
            EmployeeBehaviorSaveDTO dto,
            EmployeeBehaviorRecord record
    ) {
        record.setPeriodStart(dto.getPeriodStart());
        record.setPeriodEnd(dto.getPeriodEnd());

        record.setPerformanceScore(
                dto.getPerformanceScore()
        );
        record.setPerformanceSummary(
                normalize(dto.getPerformanceSummary())
        );
        record.setTaskCompletionRate(
                dto.getTaskCompletionRate()
        );

        record.setLateCount(
                dto.getLateCount() == null
                        ? 0 : dto.getLateCount()
        );
        record.setAbsenceDays(
                defaultZero(dto.getAbsenceDays())
        );
        record.setLeaveDays(
                defaultZero(dto.getLeaveDays())
        );
        record.setOvertimeHours(
                defaultZero(dto.getOvertimeHours())
        );

        record.setAttendanceScore(
                dto.getAttendanceScore()
        );
        record.setAttendanceSummary(
                normalize(dto.getAttendanceSummary())
        );

        record.setSatisfactionScore(
                dto.getSatisfactionScore()
        );
        record.setFeedbackText(
                normalize(dto.getFeedbackText())
        );

        if (StringUtils.hasText(dto.getSourceType())) {
            record.setSourceType(dto.getSourceType());
        } else if (!StringUtils.hasText(
                record.getSourceType()
        )) {
            record.setSourceType(
                    BehaviorSourceType.HR_INPUT.name()
            );
        }
    }

    private void validatePeriod(
            Long employeeId,
            Long excludeRecordId,
            EmployeeBehaviorSaveDTO dto
    ) {
        if (dto.getPeriodStart().isAfter(
                dto.getPeriodEnd()
        )) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "周期开始日期不能晚于结束日期"
            );
        }

        LambdaQueryWrapper<EmployeeBehaviorRecord> wrapper =
                new LambdaQueryWrapper
                        <EmployeeBehaviorRecord>()
                        .eq(
                                EmployeeBehaviorRecord
                                        ::getEmployeeId,
                                employeeId
                        )
                        .le(
                                EmployeeBehaviorRecord
                                        ::getPeriodStart,
                                dto.getPeriodEnd()
                        )
                        .ge(
                                EmployeeBehaviorRecord
                                        ::getPeriodEnd,
                                dto.getPeriodStart()
                        );

        if (excludeRecordId != null) {
            wrapper.ne(
                    EmployeeBehaviorRecord::getId,
                    excludeRecordId
            );
        }

        if (behaviorMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "该周期与已有行为记录重叠"
            );
        }
    }

    private void validateConfirmable(
            EmployeeBehaviorRecord record
    ) {
        if (record.getPerformanceScore() == null
                || record.getAttendanceScore() == null
                || record.getSatisfactionScore() == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "确认前必须填写绩效、考勤和满意度评分"
            );
        }
    }

    private EmployeeProfile requireEmployee(
            Long employeeId
    ) {
        if (employeeId == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "员工ID不能为空"
            );
        }

        EmployeeProfile employee =
                employeeMapper.selectById(employeeId);

        if (employee == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "员工档案不存在"
            );
        }

        if ("LEFT".equals(employee.getStatus())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "已离职员工不能新增行为记录"
            );
        }

        return employee;
    }

    private EmployeeBehaviorRecord requireRecord(
            Long employeeId,
            Long recordId
    ) {
        requireEmployee(employeeId);

        EmployeeBehaviorRecord record =
                behaviorMapper.selectById(recordId);

        if (record == null
                || !employeeId.equals(
                        record.getEmployeeId()
                )) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "员工行为记录不存在"
            );
        }

        return record;
    }

    private EmployeeBehaviorRecordVO toVO(
            EmployeeBehaviorRecord record
    ) {
        EmployeeBehaviorRecordVO vo =
                new EmployeeBehaviorRecordVO();

        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private BigDecimal defaultZero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private void requireStaffRole() {
        if (!STAFF_ROLES.contains(
                UserContext.getRoleCode()
        )) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "只有HR或管理员可以维护员工行为数据"
            );
        }
    }
}
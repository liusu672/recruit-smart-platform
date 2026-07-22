package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.EmployeeQueryDTO;
import com.recruit.biz.dto.EmployeeRiskDataUpdateDTO;
import com.recruit.biz.dto.EmployeeStatusUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.EmployeeProfile;
import com.recruit.biz.enums.CandidateStatus;
import com.recruit.biz.enums.EmployeeStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.EmployeeProfileMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.EmployeeProfileService;
import com.recruit.biz.vo.EmployeeDetailVO;
import com.recruit.biz.vo.EmployeeSummaryVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    @Resource
    private EmployeeProfileMapper employeeProfileMapper;

    @Resource
    private CandidateMapper candidateMapper;

    @Override
    public PageResult<EmployeeSummaryVO> listEmployees(EmployeeQueryDTO dto) {
        requireStaffRole();
        EmployeeQueryDTO query = dto == null ? new EmployeeQueryDTO() : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1
                : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10
                : query.getPageSize();

        LambdaQueryWrapper<EmployeeProfile> wrapper =
                new LambdaQueryWrapper<EmployeeProfile>()
                        .orderByDesc(EmployeeProfile::getEntryDate)
                        .orderByDesc(EmployeeProfile::getId);

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(condition -> condition
                    .like(EmployeeProfile::getEmployeeNo, keyword)
                    .or()
                    .like(EmployeeProfile::getName, keyword)
                    .or()
                    .like(EmployeeProfile::getPhone, keyword)
                    .or()
                    .like(EmployeeProfile::getEmail, keyword)
            );
        }
        if (query.getDepartment() != null
                && !query.getDepartment().isBlank()) {
            wrapper.eq(
                    EmployeeProfile::getDepartment,
                    query.getDepartment().trim()
            );
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(EmployeeProfile::getStatus, query.getStatus());
        }

        Page<EmployeeProfile> result = employeeProfileMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );
        List<EmployeeSummaryVO> records = result.getRecords()
                .stream()
                .map(this::toSummaryVO)
                .toList();

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public EmployeeDetailVO getDetail(Long id) {
        requireStaffRole();
        EmployeeProfile employee = requireEmployee(id);

        EmployeeDetailVO vo = new EmployeeDetailVO();
        vo.setId(employee.getId());
        vo.setUserId(employee.getUserId());
        vo.setCandidateId(employee.getCandidateId());
        vo.setOnboardingId(employee.getOnboardingId());
        vo.setEmployeeNo(employee.getEmployeeNo());
        vo.setName(employee.getName());
        vo.setPhone(employee.getPhone());
        vo.setEmail(employee.getEmail());
        vo.setDepartment(employee.getDepartment());
        vo.setPosition(employee.getPosition());
        vo.setEntryDate(employee.getEntryDate());
        vo.setStatus(employee.getStatus());
        vo.setPerformanceSummary(employee.getPerformanceSummary());
        vo.setPerformanceScore(employee.getPerformanceScore());
        vo.setAttendanceSummary(employee.getAttendanceSummary());
        vo.setAttendanceScore(employee.getAttendanceScore());
        vo.setSatisfactionFeedback(employee.getSatisfactionFeedback());
        vo.setSatisfactionScore(employee.getSatisfactionScore());
        vo.setTurnoverRiskLevel(employee.getTurnoverRiskLevel());
        vo.setRiskAssessedAt(employee.getRiskAssessedAt());
        vo.setCreatedAt(employee.getCreatedAt());
        vo.setUpdatedAt(employee.getUpdatedAt());

        EmployeeStatus status = EmployeeStatus.fromCode(employee.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, EmployeeStatusUpdateDTO dto) {
        requireStaffRole();
        EmployeeProfile employee = requireEmployee(id);
        String currentStatus = employee.getStatus();
        String targetStatus = dto.getStatus();

        if (targetStatus.equals(currentStatus)) {
            return;
        }

        boolean allowed = EmployeeStatus.PROBATION.name()
                .equals(currentStatus)
                && (EmployeeStatus.ACTIVE.name().equals(targetStatus)
                || EmployeeStatus.LEFT.name().equals(targetStatus))
                || EmployeeStatus.ACTIVE.name().equals(currentStatus)
                && EmployeeStatus.LEFT.name().equals(targetStatus);
        if (!allowed) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "不允许从当前员工状态修改为目标状态"
            );
        }

        int employeeUpdated = employeeProfileMapper.update(
                null,
                new LambdaUpdateWrapper<EmployeeProfile>()
                        .eq(EmployeeProfile::getId, id)
                        .eq(EmployeeProfile::getStatus, currentStatus)
                        .set(EmployeeProfile::getStatus, targetStatus)
        );
        if (employeeUpdated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "修改员工状态失败，记录可能已被其他人处理"
            );
        }

        if (EmployeeStatus.LEFT.name().equals(targetStatus)) {
            Candidate candidate = candidateMapper.selectById(
                    employee.getCandidateId()
            );
            if (candidate == null) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "员工关联的候选人不存在"
                );
            }

            int candidateUpdated = candidateMapper.update(
                    null,
                    new LambdaUpdateWrapper<Candidate>()
                            .eq(Candidate::getId, candidate.getId())
                            .set(
                                    Candidate::getCurrentStatus,
                                    CandidateStatus.AVAILABLE.name()
                            )
            );
            if (candidateUpdated != 1) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "更新候选人状态失败"
                );
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRiskData(Long id, EmployeeRiskDataUpdateDTO dto) {
        requireStaffRole();
        requireEmployee(id);

        int updated = employeeProfileMapper.update(
                null,
                new LambdaUpdateWrapper<EmployeeProfile>()
                        .eq(EmployeeProfile::getId, id)
                        .set(
                                EmployeeProfile::getPerformanceSummary,
                                dto.getPerformanceSummary().trim()
                        )
                        .set(
                                EmployeeProfile::getPerformanceScore,
                                dto.getPerformanceScore()
                        )
                        .set(
                                EmployeeProfile::getAttendanceSummary,
                                dto.getAttendanceSummary().trim()
                        )
                        .set(
                                EmployeeProfile::getAttendanceScore,
                                dto.getAttendanceScore()
                        )
                        .set(
                                EmployeeProfile::getSatisfactionFeedback,
                                dto.getSatisfactionFeedback().trim()
                        )
                        .set(
                                EmployeeProfile::getSatisfactionScore,
                                dto.getSatisfactionScore()
                        )
                        .set(EmployeeProfile::getTurnoverRiskLevel, null)
                        .set(EmployeeProfile::getRiskAssessedAt, null)
        );
        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "保存员工离职风险分析数据失败"
            );
        }
    }

    private EmployeeSummaryVO toSummaryVO(EmployeeProfile employee) {
        EmployeeSummaryVO vo = new EmployeeSummaryVO();
        vo.setId(employee.getId());
        vo.setUserId(employee.getUserId());
        vo.setCandidateId(employee.getCandidateId());
        vo.setOnboardingId(employee.getOnboardingId());
        vo.setEmployeeNo(employee.getEmployeeNo());
        vo.setName(employee.getName());
        vo.setPhone(employee.getPhone());
        vo.setEmail(employee.getEmail());
        vo.setDepartment(employee.getDepartment());
        vo.setPosition(employee.getPosition());
        vo.setEntryDate(employee.getEntryDate());
        vo.setStatus(employee.getStatus());
        vo.setPerformanceSummary(employee.getPerformanceSummary());
        vo.setPerformanceScore(employee.getPerformanceScore());
        vo.setAttendanceSummary(employee.getAttendanceSummary());
        vo.setAttendanceScore(employee.getAttendanceScore());
        vo.setSatisfactionFeedback(employee.getSatisfactionFeedback());
        vo.setSatisfactionScore(employee.getSatisfactionScore());
        vo.setTurnoverRiskLevel(employee.getTurnoverRiskLevel());
        vo.setRiskAssessedAt(employee.getRiskAssessedAt());
        vo.setCreatedAt(employee.getCreatedAt());
        vo.setUpdatedAt(employee.getUpdatedAt());

        EmployeeStatus status = EmployeeStatus.fromCode(employee.getStatus());
        vo.setStatusText(
                status == null ? "未知状态" : status.getDescription()
        );
        return vo;
    }

    private EmployeeProfile requireEmployee(Long id) {
        EmployeeProfile employee = employeeProfileMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "员工档案不存在"
            );
        }
        return employee;
    }

    private void requireStaffRole() {
        String roleCode = UserContext.getRoleCode();
        if (!"ADMIN".equals(roleCode) && !"HR".equals(roleCode)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "没有员工档案管理权限"
            );
        }
    }
}

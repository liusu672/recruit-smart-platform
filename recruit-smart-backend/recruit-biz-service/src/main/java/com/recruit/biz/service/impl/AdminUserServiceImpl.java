package com.recruit.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.biz.dto.AdminPasswordResetDTO;
import com.recruit.biz.dto.AdminUserCreateDTO;
import com.recruit.biz.dto.AdminUserQueryDTO;
import com.recruit.biz.dto.AdminUserRoleUpdateDTO;
import com.recruit.biz.dto.AdminUserStatusUpdateDTO;
import com.recruit.biz.dto.AdminUserUpdateDTO;
import com.recruit.biz.entity.Candidate;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.enums.UserStatus;
import com.recruit.biz.mapper.CandidateMapper;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import com.recruit.biz.security.UserContext;
import com.recruit.biz.service.AdminUserService;
import com.recruit.biz.vo.AdminUserVO;
import com.recruit.biz.vo.RoleOptionVO;
import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.result.PageResult;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final String CANDIDATE_ROLE_CODE = "CANDIDATE";

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private CandidateMapper candidateMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResult<AdminUserVO> pageUsers(AdminUserQueryDTO dto) {
        AdminUserQueryDTO query = dto == null
                ? new AdminUserQueryDTO()
                : dto;
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1
                ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null
                || query.getPageSize() < 1
                || query.getPageSize() > 100
                ? 10 : query.getPageSize();

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(condition -> condition
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getPhone, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword));
        }
        if (query.getRoleId() != null) {
            wrapper.eq(SysUser::getRoleId, query.getRoleId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreatedAt)
                .orderByDesc(SysUser::getId);

        Page<SysUser> result = sysUserMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );
        Map<Long, SysRole> roleMap = loadRoleMap(result.getRecords());
        List<AdminUserVO> records = result.getRecords()
                .stream()
                .map(user -> toVO(user, roleMap.get(user.getRoleId())))
                .toList();
        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public AdminUserVO getUserDetail(Long id) {
        SysUser user = getUser(id);
        return toVO(user, sysRoleMapper.selectById(user.getRoleId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createUser(AdminUserCreateDTO dto) {
        String username = dto.getUsername().trim();
        String realName = dto.getRealName().trim();
        String phone = normalizeNullable(dto.getPhone());
        String email = normalizeNullable(dto.getEmail());

        validateUsernameAvailable(username);
        validateUserPhoneAvailable(phone, null);
        SysRole role = getRole(dto.getRoleId());
        if (isCandidateRole(role)) {
            validateCandidatePhoneAvailable(phone, null);
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRoleId(role.getId());
        user.setStatus(UserStatus.ENABLED.getCode());
        sysUserMapper.insert(user);

        if (isCandidateRole(role)) {
            createCandidateProfile(user);
        }
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUser(Long id, AdminUserUpdateDTO dto) {
        if (dto.getRealName() == null
                && dto.getPhone() == null
                && dto.getEmail() == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "没有需要修改的用户信息"
            );
        }

        SysUser user = getUser(id);
        Candidate candidate = findCandidateByUserId(id);

        if (dto.getRealName() != null) {
            if (!StringUtils.hasText(dto.getRealName())) {
                throw new BusinessException(
                        ErrorCode.PARAM_ERROR,
                        "真实姓名不能为空"
                );
            }
            String realName = dto.getRealName().trim();
            user.setRealName(realName);
            if (candidate != null) {
                candidate.setName(realName);
            }
        }

        if (dto.getPhone() != null) {
            String phone = normalizeNullable(dto.getPhone());
            validateUserPhoneAvailable(phone, id);
            validateCandidatePhoneAvailable(
                    phone,
                    candidate == null ? null : candidate.getId()
            );
            user.setPhone(phone);
            if (candidate != null) {
                candidate.setPhone(phone);
            }
        }

        if (dto.getEmail() != null) {
            String email = normalizeNullable(dto.getEmail());
            user.setEmail(email);
            if (candidate != null) {
                candidate.setEmail(email);
            }
        }

        sysUserMapper.updateById(user);
        if (candidate != null) {
            candidateMapper.updateById(candidate);
        }
    }

    @Override
    public void updateStatus(Long id, AdminUserStatusUpdateDTO dto) {
        SysUser user = getUser(id);
        if (id.equals(UserContext.getUserId())
                && UserStatus.DISABLED.getCode().equals(dto.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "不能禁用当前登录账号"
            );
        }
        user.setStatus(dto.getStatus());
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRole(Long id, AdminUserRoleUpdateDTO dto) {
        if (id.equals(UserContext.getUserId())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "不能修改当前登录账号的角色"
            );
        }

        SysUser user = getUser(id);
        SysRole oldRole = getRole(user.getRoleId());
        SysRole newRole = getRole(dto.getRoleId());
        if (oldRole.getId().equals(newRole.getId())) {
            return;
        }

        if (isCandidateRole(oldRole) && !isCandidateRole(newRole)) {
            detachCandidateProfile(user.getId());
        } else if (!isCandidateRole(oldRole) && isCandidateRole(newRole)) {
            bindOrCreateCandidateProfile(user);
        }

        user.setRoleId(newRole.getId());
        sysUserMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, AdminPasswordResetDTO dto) {
        if (id.equals(UserContext.getUserId())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "请通过个人中心修改当前账号密码"
            );
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "两次输入的新密码不一致"
            );
        }

        SysUser user = getUser(id);
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public List<RoleOptionVO> listRoles() {
        return sysRoleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>()
                                .orderByAsc(SysRole::getId)
                )
                .stream()
                .map(this::toRoleOptionVO)
                .toList();
    }

    private Map<Long, SysRole> loadRoleMap(List<SysUser> users) {
        Set<Long> roleIds = users.stream()
                .map(SysUser::getRoleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds)
                .stream()
                .collect(Collectors.toMap(
                        SysRole::getId,
                        Function.identity()
                ));
    }

    private void createCandidateProfile(SysUser user) {
        Candidate candidate = new Candidate();
        candidate.setUserId(user.getId());
        candidate.setName(user.getRealName());
        candidate.setPhone(user.getPhone());
        candidate.setEmail(user.getEmail());
        candidate.setYearsOfExperience(0);
        candidate.setCurrentStatus("AVAILABLE");
        candidate.setSource("ADMIN_CREATE");
        candidate.setCreatedBy(UserContext.getUserId());
        candidateMapper.insert(candidate);
    }

    private void bindOrCreateCandidateProfile(SysUser user) {
        Candidate linkedCandidate = findCandidateByUserId(user.getId());
        if (linkedCandidate != null) {
            syncCandidateIdentity(linkedCandidate, user);
            candidateMapper.updateById(linkedCandidate);
            return;
        }

        Candidate candidate = null;
        if (StringUtils.hasText(user.getPhone())) {
            candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(Candidate::getPhone, user.getPhone())
            );
        }
        if (candidate == null) {
            createCandidateProfile(user);
            return;
        }
        if (candidate.getUserId() != null
                && !candidate.getUserId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "手机号已绑定其他候选人账号"
            );
        }

        candidate.setUserId(user.getId());
        syncCandidateIdentity(candidate, user);
        candidateMapper.updateById(candidate);
    }

    private void detachCandidateProfile(Long userId) {
        Candidate candidate = findCandidateByUserId(userId);
        if (candidate == null) {
            return;
        }
        candidate.setUserId(null);
        candidateMapper.updateById(candidate);
    }

    private void syncCandidateIdentity(Candidate candidate, SysUser user) {
        candidate.setName(user.getRealName());
        candidate.setPhone(user.getPhone());
        candidate.setEmail(user.getEmail());
    }

    private Candidate findCandidateByUserId(Long userId) {
        return candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getUserId, userId)
        );
    }

    private void validateUsernameAvailable(String username) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        if (count > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "用户名已存在"
            );
        }
    }

    private void validateUserPhoneAvailable(String phone, Long excludedUserId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone);
        if (excludedUserId != null) {
            wrapper.ne(SysUser::getId, excludedUserId);
        }
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "手机号已被其他用户使用"
            );
        }
    }

    private void validateCandidatePhoneAvailable(
            String phone,
            Long excludedCandidateId
    ) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        LambdaQueryWrapper<Candidate> wrapper =
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getPhone, phone);
        if (excludedCandidateId != null) {
            wrapper.ne(Candidate::getId, excludedCandidateId);
        }
        if (candidateMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR,
                    "手机号已被其他候选人使用"
            );
        }
    }

    private SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private SysRole getRole(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }
        return role;
    }

    private boolean isCandidateRole(SysRole role) {
        return CANDIDATE_ROLE_CODE.equals(role.getRoleCode());
    }

    private AdminUserVO toVO(SysUser user, SysRole role) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRoleId(user.getRoleId());
        if (role != null) {
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
        }
        vo.setStatus(user.getStatus());
        UserStatus status = UserStatus.fromCode(user.getStatus());
        vo.setStatusText(status == null ? "未知状态" : status.getDescription());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    private RoleOptionVO toRoleOptionVO(SysRole role) {
        RoleOptionVO vo = new RoleOptionVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        return vo;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

package com.recruit.biz.service;

import com.recruit.biz.dto.AdminPasswordResetDTO;
import com.recruit.biz.dto.AdminUserCreateDTO;
import com.recruit.biz.dto.AdminUserQueryDTO;
import com.recruit.biz.dto.AdminUserRoleUpdateDTO;
import com.recruit.biz.dto.AdminUserStatusUpdateDTO;
import com.recruit.biz.dto.AdminUserUpdateDTO;
import com.recruit.biz.vo.AdminUserVO;
import com.recruit.biz.vo.RoleOptionVO;
import com.recruit.common.result.PageResult;

import java.util.List;

public interface AdminUserService {

    PageResult<AdminUserVO> pageUsers(AdminUserQueryDTO dto);

    AdminUserVO getUserDetail(Long id);

    Long createUser(AdminUserCreateDTO dto);

    void updateUser(Long id, AdminUserUpdateDTO dto);

    void updateStatus(Long id, AdminUserStatusUpdateDTO dto);

    void updateRole(Long id, AdminUserRoleUpdateDTO dto);

    void resetPassword(Long id, AdminPasswordResetDTO dto);

    List<RoleOptionVO> listRoles();
}

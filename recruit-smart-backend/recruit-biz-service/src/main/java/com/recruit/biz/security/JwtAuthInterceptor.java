package com.recruit.biz.security;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import com.recruit.common.util.JwtUtil;
import com.recruit.biz.entity.SysRole;
import com.recruit.biz.entity.SysUser;
import com.recruit.biz.mapper.SysRoleMapper;
import com.recruit.biz.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    //角色校验方法
    private void checkRole(Object handler, String roleCode) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        RequireRoles requireRoles =
                handlerMethod.getMethodAnnotation(
                        RequireRoles.class
                );

        if (requireRoles == null) {
            requireRoles = handlerMethod
                    .getBeanType()
                    .getAnnotation(RequireRoles.class);
        }

        if (requireRoles == null) {
            return;
        }

        boolean allowed = Arrays.stream(requireRoles.value())
                .anyMatch(role -> role.equals(roleCode));

        if (!allowed) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "没有操作权限"
            );
        }
    }
    @Override
    //Controller 执行前
    public boolean preHandle(
            HttpServletRequest request,//请求头
            HttpServletResponse response,//必要时直接响应
            Object handler//当前准备执行的 Controller 方法
    ) {
        // 清除线程可能残留的数据
        UserContext.clear();

        // 跨域预检请求不需要认证
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization =
                request.getHeader("Authorization");//客户端传递形式：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        if (!StringUtils.hasText(authorization)
                || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "未登录或登录状态失效"
            );
        }

        String token = authorization.substring(7).trim();

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "未登录或登录状态失效"
            );
        }

        Claims claims = jwtUtil.parseToken(token);

        Object userIdClaim = claims.get("userId");

        if (userIdClaim == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "Token中的用户信息不完整"
            );
        }

        Long userId;

        try {
            userId = Long.valueOf(userIdClaim.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "Token中的用户信息不正确"
                );
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "用户不存在或已被禁用"
            );
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        if (role == null || !StringUtils.hasText(role.getRoleCode())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "用户角色不存在"
            );
        }

        String roleCode = role.getRoleCode();
        checkRole(handler, roleCode);

        CurrentUser currentUser = new CurrentUser(
                userId,
                user.getUsername(),
                roleCode
        );

        UserContext.set(currentUser);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        UserContext.clear();
    }
}

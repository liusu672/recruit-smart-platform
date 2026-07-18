package com.recruit.biz.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Integer status;
    private String statusText;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

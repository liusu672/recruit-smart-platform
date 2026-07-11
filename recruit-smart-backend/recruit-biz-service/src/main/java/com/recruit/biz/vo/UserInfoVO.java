package com.recruit.biz.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String roleCode;
    private String roleName;
    private Long roleId;
    private Integer status;
}

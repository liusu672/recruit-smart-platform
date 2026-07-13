package com.recruit.biz.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private String tokenType="Bearer";
    private UserInfoVO userInfo;
}

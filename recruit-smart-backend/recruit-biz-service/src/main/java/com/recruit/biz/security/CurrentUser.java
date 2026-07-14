package com.recruit.biz.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentUser {
    private Long userId;
    private String username;
    private String roleCode;
}

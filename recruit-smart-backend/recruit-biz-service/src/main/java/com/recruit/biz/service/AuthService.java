package com.recruit.biz.service;

import com.recruit.biz.dto.LoginDTO;
import com.recruit.biz.vo.LoginVO;

public interface AuthService {
    public LoginVO login(LoginDTO dto);
}

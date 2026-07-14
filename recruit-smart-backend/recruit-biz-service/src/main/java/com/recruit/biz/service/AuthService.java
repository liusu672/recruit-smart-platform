package com.recruit.biz.service;

import com.recruit.biz.dto.CandidateRegisterDTO;
import com.recruit.biz.dto.LoginDTO;
import com.recruit.biz.vo.LoginVO;
import com.recruit.biz.vo.UserInfoVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
    UserInfoVO getCurrentUser();
    LoginVO register(CandidateRegisterDTO dto);
}

package com.recruit.biz.service;

import com.recruit.biz.dto.PasswordUpdateDTO;
import com.recruit.biz.dto.UserProfileUpdateDTO;
import com.recruit.biz.vo.InterviewerOptionVO;

import java.util.List;

public interface UserService {

    void updateCurrentUser(UserProfileUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto);

    List<InterviewerOptionVO> listInterviewers();
}

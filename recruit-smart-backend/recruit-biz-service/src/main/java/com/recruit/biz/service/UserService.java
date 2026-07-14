package com.recruit.biz.service;

import com.recruit.biz.dto.PasswordUpdateDTO;
import com.recruit.biz.dto.UserProfileUpdateDTO;

public interface UserService {

    void updateCurrentUser(UserProfileUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto);
}

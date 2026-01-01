package com.sky.service;

import com.sky.dto.user.UserLoginDTO;
import com.sky.vo.user.UserLoginVO;

public interface UserService {

    UserLoginVO login (UserLoginDTO dto);
}

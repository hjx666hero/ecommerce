package com.ecommerce.mall.service;

import com.ecommerce.mall.entity.Address;
import com.ecommerce.mall.vo.UserVO;

public interface UserService {
    UserVO getProfile(Long userId);
    void updateProfile(Long userId, String nickname, String avatar, Integer gender);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.LoginRequest;
import com.freedoc.dto.LoginResponse;
import com.freedoc.dto.RegisterRequest;
import com.freedoc.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    String register(RegisterRequest request);

    User getByAccount(String account);

    /**
     * 更新用户信息（用户名、头像）
     */
    User updateUserInfo(String userId, Map<String, Object> updates);

    /**
     * 修改密码
     */
    void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 搜索用户（用于邀请成员）
     */
    List<User> searchUsers(String keyword);

}

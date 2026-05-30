package com.freedoc.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.util.JwtUtil;
import com.freedoc.common.util.PasswordUtil;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.LoginRequest;
import com.freedoc.dto.LoginResponse;
import com.freedoc.dto.RegisterRequest;
import com.freedoc.dto.TeamCreateRequest;
import com.freedoc.entity.User;
import com.freedoc.mapper.UserMapper;
import com.freedoc.service.TeamService;
import com.freedoc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final TeamService teamService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getByAccount(request.getAccount());
        if (user == null) {
            throw new BusinessException("error.user.accountNotFound");
        }

        if (!PasswordUtil.verify(request.getPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException("error.user.passwordWrong");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());
        return new LoginResponse(token, user.getUserId(), user.getUserName(), user.getUserIcon());
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        User existUser = getByAccount(request.getAccount());
        if (existUser != null) {
            throw new BusinessException("error.user.accountExists");
        }

        String salt = RandomUtil.randomString(5);
        String encryptedPassword = PasswordUtil.encrypt(request.getPassword(), salt);

        User user = new User();
        user.setUserId(SnowflakeIdUtil.nextId());
        user.setUserName(request.getUserName() != null && !request.getUserName().isEmpty() 
            ? request.getUserName() 
            : request.getAccount()); // 如果没有提供用户名，使用账号作为用户名
        user.setAccount(request.getAccount());
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        user.setUserIcon(request.getUserIcon() != null && !request.getUserIcon().isEmpty()
            ? request.getUserIcon()
            : "fa-solid fa-user");
        user.setCreateTime(LocalDateTime.now());

        save(user);

        TeamCreateRequest teamRequest = new TeamCreateRequest();
        teamRequest.setTeamName(I18nUtil.getMessage("common.defaultTeam"));
        teamRequest.setTeamIcon("fa-solid fa-users");
        teamService.createTeam(teamRequest, user.getUserId());

        return user.getUserId();
    }

    @Override
    public User getByAccount(String account) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
    }

    @Override
    public User updateUserInfo(String userId, Map<String, Object> updates) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("error.user.notFound");
        }
        if (updates.containsKey("userName")) {
            user.setUserName((String) updates.get("userName"));
        }
        if (updates.containsKey("userIcon")) {
            user.setUserIcon((String) updates.get("userIcon"));
        }
        updateById(user);
        // 返回时脱敏
        user.setPassword(null);
        user.setSalt(null);
        return user;
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("error.user.notFound");
        }
        if (!PasswordUtil.verify(oldPassword, user.getSalt(), user.getPassword())) {
            throw new BusinessException("error.user.oldPasswordWrong");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException("error.user.newPasswordSameAsOld");
        }

        String encryptedPassword = PasswordUtil.encrypt(newPassword, user.getSalt());
        user.setPassword(encryptedPassword);
        updateById(user);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return list(new LambdaQueryWrapper<User>()
                .like(User::getUserName, keyword)
                .or()
                .like(User::getAccount, keyword)
                .last("LIMIT 20")
        ).stream().peek(u -> {
            u.setPassword(null);
            u.setSalt(null);
        }).toList();
    }

}

package com.recruit.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.api.ResultCode;
import com.recruit.common.constant.CommonConstants;
import com.recruit.common.exception.BizException;
import com.recruit.user.dto.UserDtos.LoginRequest;
import com.recruit.user.dto.UserDtos.LoginResponse;
import com.recruit.user.dto.UserDtos.RegisterRequest;
import com.recruit.user.entity.User;
import com.recruit.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户业务：注册、登录（Redis Token）、查询、校验
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redis;

    public UserService(UserMapper userMapper, StringRedisTemplate redis) {
        this.userMapper = userMapper;
        this.redis = redis;
    }

    public void register(RegisterRequest req) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.USER_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setPassword(md5(req.getPassword()));
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(req.getUsername());
        }
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (user == null || !user.getPassword().equals(md5(req.getPassword()))) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        // Redis 存储：recruit:token:{token} -> userId:role，2 小时有效
        redis.opsForValue().set(CommonConstants.TOKEN_KEY_PREFIX + token,
                user.getId() + ":" + user.getRole(), 2, TimeUnit.HOURS);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setRole(user.getRole());
        resp.setNickname(user.getNickname());
        resp.setCompany(user.getCompany());
        return resp;
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            redis.delete(CommonConstants.TOKEN_KEY_PREFIX + token);
        }
    }

    public User info(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }

    public Long validate(String token) {
        String value = redis.opsForValue().get(CommonConstants.TOKEN_KEY_PREFIX + token);
        if (value == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return Long.valueOf(value.split(":")[0]);
    }

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}

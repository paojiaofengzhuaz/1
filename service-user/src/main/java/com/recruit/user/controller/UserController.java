package com.recruit.user.controller;

import com.recruit.common.api.Result;
import com.recruit.common.constant.CommonConstants;
import com.recruit.user.dto.UserDtos.LoginRequest;
import com.recruit.user.dto.UserDtos.LoginResponse;
import com.recruit.user.dto.UserDtos.RegisterRequest;
import com.recruit.user.entity.User;
import com.recruit.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterRequest req) {
        userService.register(req);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = CommonConstants.TOKEN_HEADER, required = false) String token) {
        userService.logout(token);
        return Result.ok();
    }

    @GetMapping("/info")
    public Result<User> info(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.ok(userService.info(userId));
    }

    @GetMapping("/validate")
    public Result<Long> validate(@RequestParam("token") String token) {
        return Result.ok(userService.validate(token));
    }
}

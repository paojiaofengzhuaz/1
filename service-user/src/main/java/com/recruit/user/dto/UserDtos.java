package com.recruit.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户服务 DTO 集合
 */
public class UserDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度为 3-20 位")
        private String username;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
        private String password;
        private String nickname;
        private String phone;
        @NotNull(message = "请选择身份")
        private Integer role;
        private String company;
        private String position;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Long userId;
        private Integer role;
        private String nickname;
        private String company;
    }
}

package com.recruit.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户服务：注册 / 登录 / Token 管理
 * scanBasePackages 扫描 com.recruit，使公共模块的统一异常处理与分页插件生效
 */
@SpringBootApplication(scanBasePackages = "com.recruit")
@MapperScan("com.recruit.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}

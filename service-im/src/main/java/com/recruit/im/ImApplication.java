package com.recruit.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线沟通服务：会话列表 + 消息收发（前端轮询拉取）
 */
@SpringBootApplication(scanBasePackages = "com.recruit")
@MapperScan("com.recruit.im.mapper")
public class ImApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
    }
}

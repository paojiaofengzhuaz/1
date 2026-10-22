package com.recruit.resume;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 简历服务：简历维护 + 简历投递
 */
@SpringBootApplication(scanBasePackages = "com.recruit")
@MapperScan("com.recruit.resume.mapper")
public class ResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeApplication.class, args);
    }
}

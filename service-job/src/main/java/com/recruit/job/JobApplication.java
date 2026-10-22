package com.recruit.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 岗位服务：岗位发布、检索、详情
 */
@SpringBootApplication(scanBasePackages = "com.recruit")
@MapperScan("com.recruit.job.mapper")
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}

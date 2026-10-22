package com.recruit.match;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 智能匹配服务：基于简历技能/城市/薪资/学历与岗位进行规则匹配，并提供关键词助手
 * <p>本服务不访问数据库（数据均通过 Feign 调用其他服务），因此排除数据源自动配置</p>
 */
@SpringBootApplication(
        scanBasePackages = "com.recruit",
        exclude = {DataSourceAutoConfiguration.class, MybatisPlusAutoConfiguration.class})
@EnableFeignClients(basePackages = "com.recruit.common.feign")
public class MatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchApplication.class, args);
    }
}

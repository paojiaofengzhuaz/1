package com.recruit.common.feign;

import com.recruit.common.api.Result;
import com.recruit.common.dto.ResumeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 简历服务 Feign 客户端（供智能匹配服务调用）
 */
@FeignClient(name = "service-resume")
public interface ResumeClient {

    @GetMapping("/resume/by-user")
    Result<ResumeDTO> byUser(@RequestParam("userId") Long userId);
}

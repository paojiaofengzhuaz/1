package com.recruit.common.feign;

import com.recruit.common.api.Result;
import com.recruit.common.dto.JobDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 岗位服务 Feign 客户端（供智能匹配服务调用）
 */
@FeignClient(name = "service-job")
public interface JobClient {

    @GetMapping("/job/all")
    Result<List<JobDTO>> listAll();

    @GetMapping("/job/{id}")
    Result<JobDTO> detail(@PathVariable("id") Long id);
}

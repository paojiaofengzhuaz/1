package com.recruit.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.api.Result;
import com.recruit.common.api.ResultCode;
import com.recruit.common.constant.CommonConstants;
import com.recruit.common.dto.JobDTO;
import com.recruit.common.exception.BizException;
import com.recruit.job.entity.Job;
import com.recruit.job.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 岗位接口
 */
@RestController
@RequestMapping("/job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** 分页搜索（公开）：支持行业、城市、经验、学历、薪资、排序多条件筛选 */
    @GetMapping("/list")
    public Result<Page<Job>> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "9") int size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String category,
                                  @RequestParam(required = false) String city,
                                  @RequestParam(required = false) String education,
                                  @RequestParam(required = false) String experience,
                                  @RequestParam(required = false) Integer salaryMin,
                                  @RequestParam(required = false) Integer salaryMax,
                                  @RequestParam(required = false) String sort) {
        return Result.ok(jobService.list(page, size, keyword, category, city,
                education, experience, salaryMin, salaryMax, sort));
    }

    /** 筛选项统计（公开）：行业 / 城市及其岗位数 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(jobService.stats());
    }

    /** 热门企业（公开）：按在招岗位数聚合 */
    @GetMapping("/companies")
    public Result<List<Map<String, Object>>> companies(@RequestParam(defaultValue = "9") int limit) {
        return Result.ok(jobService.companies(limit));
    }

    /** 热门岗位（公开） */
    @GetMapping("/hot")
    public Result<List<Job>> hot() {
        return Result.ok(jobService.hot());
    }

    /** 岗位详情（公开） */
    @GetMapping("/{id}")
    public Result<Job> detail(@PathVariable("id") Long id) {
        return Result.ok(jobService.detail(id));
    }

    /** 发布岗位（仅招聘者） */
    @PostMapping("/publish")
    public Result<Job> publish(@RequestHeader(CommonConstants.USER_ID_HEADER) Long hrId,
                               @RequestHeader(CommonConstants.ROLE_HEADER) Integer role,
                               @RequestBody Job job) {
        checkRecruiter(role);
        return Result.ok(jobService.publish(hrId, job.getHrName(), job));
    }

    /** 我发布的岗位（招聘者） */
    @GetMapping("/my")
    public Result<List<Job>> my(@RequestHeader(CommonConstants.USER_ID_HEADER) Long hrId,
                                @RequestHeader(CommonConstants.ROLE_HEADER) Integer role) {
        checkRecruiter(role);
        return Result.ok(jobService.my(hrId));
    }

    /** 下线岗位 */
    @PostMapping("/offline/{id}")
    public Result<Void> offline(@RequestHeader(CommonConstants.USER_ID_HEADER) Long hrId,
                                @RequestHeader(CommonConstants.ROLE_HEADER) Integer role,
                                @PathVariable("id") Long id) {
        checkRecruiter(role);
        jobService.offline(hrId, id);
        return Result.ok();
    }

    /** 内部接口：智能匹配服务获取全部在招岗位 */
    @GetMapping("/all")
    public Result<List<JobDTO>> all() {
        return Result.ok(jobService.allForMatch());
    }

    private void checkRecruiter(Integer role) {
        if (role == null || role != CommonConstants.ROLE_RECRUITER) {
            throw new BizException(ResultCode.ROLE_ERROR);
        }
    }
}

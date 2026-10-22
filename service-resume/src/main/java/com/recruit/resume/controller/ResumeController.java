package com.recruit.resume.controller;

import com.recruit.common.api.Result;
import com.recruit.common.constant.CommonConstants;
import com.recruit.common.dto.ResumeDTO;
import com.recruit.resume.entity.Resume;
import com.recruit.resume.service.ResumeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简历接口
 */
@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /** 查询当前用户简历（可能为 null，前端据此提示完善简历） */
    @GetMapping("")
    public Result<Resume> mine(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.ok(resumeService.getByUser(userId));
    }

    /** 保存/更新简历 */
    @PostMapping("/save")
    public Result<Resume> save(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                               @RequestBody Resume resume) {
        return Result.ok(resumeService.save(userId, resume));
    }

    /** 内部接口：智能匹配服务按用户ID获取简历 */
    @GetMapping("/by-user")
    public Result<ResumeDTO> byUser(@RequestParam("userId") Long userId) {
        return Result.ok(resumeService.byUserForMatch(userId));
    }
}

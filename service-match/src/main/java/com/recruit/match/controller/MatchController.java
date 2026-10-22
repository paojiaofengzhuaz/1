package com.recruit.match.controller;

import com.recruit.common.api.Result;
import com.recruit.common.constant.CommonConstants;
import com.recruit.match.dto.MatchDtos.AssistantResult;
import com.recruit.match.dto.MatchDtos.MatchItem;
import com.recruit.match.service.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 智能匹配接口
 */
@RestController
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /** 岗位智能匹配（需登录，基于当前用户简历） */
    @GetMapping("/jobs")
    public Result<List<MatchItem>> jobs(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.ok(matchService.matchJobs(userId));
    }

    /** 智能匹配助手（公开）：输入自然语言描述，返回推荐岗位与话术 */
    @GetMapping("/assistant")
    public Result<AssistantResult> assistant(@RequestParam(value = "q", required = false) String q) {
        return Result.ok(matchService.assistant(q));
    }
}

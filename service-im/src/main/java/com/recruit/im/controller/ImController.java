package com.recruit.im.controller;

import com.recruit.common.api.Result;
import com.recruit.common.constant.CommonConstants;
import com.recruit.im.dto.ImDtos.SendRequest;
import com.recruit.im.dto.ImDtos.StartRequest;
import com.recruit.im.entity.Conversation;
import com.recruit.im.entity.Message;
import com.recruit.im.service.ImService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 在线沟通接口
 */
@RestController
@RequestMapping("/im")
public class ImController {

    private final ImService imService;

    public ImController(ImService imService) {
        this.imService = imService;
    }

    /** 发起沟通（返回会话） */
    @PostMapping("/start")
    public Result<Conversation> start(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                      @RequestHeader(CommonConstants.ROLE_HEADER) Integer role,
                                      @Validated @RequestBody StartRequest req) {
        return Result.ok(imService.start(userId, role, req));
    }

    /** 我的会话列表 */
    @GetMapping("/conversations")
    public Result<List<Conversation>> conversations(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId) {
        return Result.ok(imService.list(userId));
    }

    /** 会话消息（前端轮询） */
    @GetMapping("/messages")
    public Result<List<Message>> messages(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                                          @RequestParam("convId") Long convId) {
        return Result.ok(imService.messages(convId, userId));
    }

    /** 发送消息 */
    @PostMapping("/send")
    public Result<Long> send(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                             @RequestHeader(CommonConstants.ROLE_HEADER) Integer role,
                             @Validated @RequestBody SendRequest req) {
        return Result.ok(imService.send(userId, role, req));
    }

    /** 已读 */
    @PostMapping("/read")
    public Result<Void> read(@RequestHeader(CommonConstants.USER_ID_HEADER) Long userId,
                             @RequestParam("convId") Long convId) {
        imService.read(convId, userId);
        return Result.ok();
    }
}

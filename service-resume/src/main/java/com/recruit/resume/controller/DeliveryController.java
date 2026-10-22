package com.recruit.resume.controller;

import com.recruit.common.api.Result;
import com.recruit.common.api.ResultCode;
import com.recruit.common.constant.CommonConstants;
import com.recruit.common.exception.BizException;
import com.recruit.resume.entity.Delivery;
import com.recruit.resume.service.DeliveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历投递接口
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /** 投递简历（求职者） */
    @PostMapping("/send")
    public Result<Delivery> send(@RequestHeader(CommonConstants.USER_ID_HEADER) Long seekerId,
                                 @RequestHeader(CommonConstants.ROLE_HEADER) Integer role,
                                 @RequestBody Delivery req) {
        if (role == null || role != CommonConstants.ROLE_SEEKER) {
            throw new BizException(ResultCode.ROLE_ERROR);
        }
        return Result.ok(deliveryService.send(seekerId, req));
    }

    /** 我投递的简历（求职者） */
    @GetMapping("/my")
    public Result<List<Delivery>> my(@RequestHeader(CommonConstants.USER_ID_HEADER) Long seekerId) {
        return Result.ok(deliveryService.my(seekerId));
    }

    /** 收到的投递（招聘者） */
    @GetMapping("/received")
    public Result<List<Delivery>> received(@RequestHeader(CommonConstants.USER_ID_HEADER) Long hrId,
                                           @RequestHeader(CommonConstants.ROLE_HEADER) Integer role) {
        if (role == null || role != CommonConstants.ROLE_RECRUITER) {
            throw new BizException(ResultCode.ROLE_ERROR);
        }
        return Result.ok(deliveryService.received(hrId));
    }

    /** 更新投递状态：1已查看 2邀面试 3不合适 */
    @PostMapping("/status")
    public Result<Void> status(@RequestHeader(CommonConstants.USER_ID_HEADER) Long hrId,
                               @RequestParam("id") Long id,
                               @RequestParam("status") Integer status) {
        deliveryService.updateStatus(hrId, id, status);
        return Result.ok();
    }
}

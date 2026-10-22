package com.recruit.resume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.api.ResultCode;
import com.recruit.common.exception.BizException;
import com.recruit.resume.entity.Delivery;
import com.recruit.resume.entity.Resume;
import com.recruit.resume.mapper.DeliveryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简历投递业务
 */
@Service
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final ResumeService resumeService;

    public DeliveryService(DeliveryMapper deliveryMapper, ResumeService resumeService) {
        this.deliveryMapper = deliveryMapper;
        this.resumeService = resumeService;
    }

    public Delivery send(Long seekerId, Delivery req) {
        Resume resume = resumeService.getByUser(seekerId);
        if (resume == null) {
            throw new BizException(ResultCode.RESUME_NOT_FOUND);
        }
        Long count = deliveryMapper.selectCount(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getJobId, req.getJobId())
                .eq(Delivery::getSeekerId, seekerId));
        if (count != null && count > 0) {
            throw new BizException(ResultCode.DELIVERY_EXISTS);
        }
        req.setId(null);
        req.setResumeId(resume.getId());
        req.setSeekerId(seekerId);
        req.setSeekerName(resume.getName());
        req.setStatus(0);
        deliveryMapper.insert(req);
        return req;
    }

    public List<Delivery> my(Long seekerId) {
        return deliveryMapper.selectList(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getSeekerId, seekerId)
                .orderByDesc(Delivery::getCreateTime));
    }

    public List<Delivery> received(Long hrId) {
        return deliveryMapper.selectList(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getHrId, hrId)
                .orderByDesc(Delivery::getCreateTime));
    }

    public void updateStatus(Long hrId, Long id, Integer status) {
        Delivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!hrId.equals(delivery.getHrId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        delivery.setStatus(status);
        deliveryMapper.updateById(delivery);
    }
}

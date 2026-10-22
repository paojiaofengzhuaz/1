package com.recruit.resume.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.api.ResultCode;
import com.recruit.common.dto.ResumeDTO;
import com.recruit.common.exception.BizException;
import com.recruit.resume.entity.Resume;
import com.recruit.resume.mapper.ResumeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 简历业务
 */
@Service
public class ResumeService {

    private final ResumeMapper resumeMapper;

    public ResumeService(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    public Resume getByUser(Long userId) {
        return resumeMapper.selectOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId)
                .orderByDesc(Resume::getId)
                .last("LIMIT 1"));
    }

    public Resume save(Long userId, Resume resume) {
        Resume existed = getByUser(userId);
        if (existed == null) {
            resume.setId(null);
            resume.setUserId(userId);
            resumeMapper.insert(resume);
            return resume;
        }
        resume.setId(existed.getId());
        resume.setUserId(userId);
        resumeMapper.updateById(resume);
        return resume;
    }

    /** 内部接口：供智能匹配服务使用 */
    public ResumeDTO byUserForMatch(Long userId) {
        Resume resume = getByUser(userId);
        if (resume == null) {
            throw new BizException(ResultCode.RESUME_NOT_FOUND);
        }
        ResumeDTO dto = new ResumeDTO();
        BeanUtils.copyProperties(resume, dto);
        return dto;
    }
}

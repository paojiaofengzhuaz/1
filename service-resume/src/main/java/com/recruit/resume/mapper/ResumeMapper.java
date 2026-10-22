package com.recruit.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.resume.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}

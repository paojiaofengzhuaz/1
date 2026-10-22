package com.recruit.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
}

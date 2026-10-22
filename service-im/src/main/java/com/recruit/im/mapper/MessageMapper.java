package com.recruit.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.im.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}

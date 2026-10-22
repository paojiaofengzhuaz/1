package com.recruit.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历投递记录
 */
@Data
@TableName("delivery")
public class Delivery {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;
    private String jobTitle;
    private String company;
    private Long resumeId;
    private Long seekerId;
    private String seekerName;
    private Long hrId;
    /** 0 待查看 1 已查看 2 邀面试 3 不合适 */
    private Integer status;
    private LocalDateTime createTime;
}

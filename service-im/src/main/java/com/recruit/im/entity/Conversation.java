package com.recruit.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体
 */
@Data
@TableName("conversation")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 求职者ID */
    private Long userA;
    /** 招聘者ID */
    private Long userB;
    private Long jobId;
    private String jobTitle;
    private String lastMsg;
    private LocalDateTime lastTime;
    private Integer unreadA;
    private Integer unreadB;
}

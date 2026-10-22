package com.recruit.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String gender;
    private Integer age;
    private String phone;
    private String email;
    private String education;
    private Integer experienceYears;
    private String city;
    private Integer salary;
    /** 技能，逗号分隔 */
    private String skills;
    private String selfIntro;
    private LocalDateTime updateTime;
}

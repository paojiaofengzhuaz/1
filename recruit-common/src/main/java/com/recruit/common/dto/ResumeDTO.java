package com.recruit.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 简历 DTO（跨服务传输 / 智能匹配使用）
 */
@Data
public class ResumeDTO implements Serializable {

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
    private String skills;
    private String selfIntro;
}

package com.recruit.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位 DTO（跨服务传输 / 智能匹配使用）
 */
@Data
public class JobDTO implements Serializable {

    private Long id;
    private String title;
    private String category;
    private String company;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String education;
    private String experience;
    private String tags;
    private String description;
    private String requirement;
    private Long hrId;
    private String hrName;
    private Integer status;
    private LocalDateTime createTime;
}

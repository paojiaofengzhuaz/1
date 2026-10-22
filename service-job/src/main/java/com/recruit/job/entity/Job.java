package com.recruit.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位实体
 */
@Data
@TableName("job")
public class Job {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    /** 行业分类，如 互联网/IT、金融/银行、医疗/医药 */
    private String category;
    private String company;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String education;
    private String experience;
    /** 技能标签，逗号分隔 */
    private String tags;
    private String description;
    private String requirement;
    private Long hrId;
    private String hrName;
    /** 1 在招 0 下线 */
    private Integer status;
    private LocalDateTime createTime;
}

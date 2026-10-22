package com.recruit.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.api.ResultCode;
import com.recruit.common.exception.BizException;
import com.recruit.job.entity.Job;
import com.recruit.job.mapper.JobMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位业务
 */
@Service
public class JobService {

    private final JobMapper jobMapper;

    public JobService(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    /**
     * 岗位分页检索
     *
     * @param category  行业分类（精确）
     * @param experience 经验要求（精确）
     * @param salaryMin 薪资区间下限（K），与岗位区间取交集
     * @param salaryMax 薪资区间上限（K）
     * @param sort      new-最新 / salary_desc-薪资从高到低 / salary_asc-薪资从低到高
     */
    public Page<Job> list(int page, int size, String keyword, String category, String city,
                          String education, String experience,
                          Integer salaryMin, Integer salaryMax, String sort) {
        LambdaQueryWrapper<Job> qw = new LambdaQueryWrapper<Job>()
                .eq(Job::getStatus, 1)
                .eq(StringUtils.hasText(category), Job::getCategory, category)
                .eq(StringUtils.hasText(city), Job::getCity, city)
                .eq(StringUtils.hasText(education), Job::getEducation, education)
                .eq(StringUtils.hasText(experience), Job::getExperience, experience)
                // 岗位薪资区间与筛选区间有交集即命中
                .ge(salaryMin != null, Job::getSalaryMax, salaryMin)
                .le(salaryMax != null, Job::getSalaryMin, salaryMax)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Job::getTitle, keyword)
                        .or().like(Job::getTags, keyword)
                        .or().like(Job::getCompany, keyword)
                        .or().like(Job::getCategory, keyword));
        if ("salary_desc".equals(sort)) {
            qw.orderByDesc(Job::getSalaryMax);
        } else if ("salary_asc".equals(sort)) {
            qw.orderByAsc(Job::getSalaryMin);
        } else {
            qw.orderByDesc(Job::getCreateTime);
        }
        qw.orderByDesc(Job::getId);
        return jobMapper.selectPage(new Page<>(page, size), qw);
    }

    /** 筛选项统计：行业（含该行业热门技能标签）、城市及各自岗位数 */
    public Map<String, Object> stats() {
        List<Job> jobs = jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getStatus, 1)
                .select(Job::getCategory, Job::getCity, Job::getTags));
        Map<String, Long> catCount = new LinkedHashMap<>();
        Map<String, Long> cityCount = new LinkedHashMap<>();
        Map<String, Map<String, Long>> catTags = new LinkedHashMap<>();
        for (Job job : jobs) {
            String cat = StringUtils.hasText(job.getCategory()) ? job.getCategory() : "其他";
            catCount.merge(cat, 1L, Long::sum);
            cityCount.merge(StringUtils.hasText(job.getCity()) ? job.getCity() : "其他", 1L, Long::sum);
            if (StringUtils.hasText(job.getTags())) {
                Map<String, Long> tagMap = catTags.computeIfAbsent(cat, k -> new LinkedHashMap<>());
                for (String tag : job.getTags().split("[,，]")) {
                    if (StringUtils.hasText(tag)) {
                        tagMap.merge(tag.trim(), 1L, Long::sum);
                    }
                }
            }
        }
        List<Map<String, Object>> categories = catCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", e.getKey());
                    item.put("count", e.getValue());
                    Map<String, Long> tags = catTags.getOrDefault(e.getKey(), java.util.Collections.emptyMap());
                    item.put("tags", tags.entrySet().stream()
                            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                            .limit(10)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList()));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", jobs.size());
        result.put("categories", categories);
        result.put("cities", toCountItems(cityCount));
        return result;
    }

    /** 热门企业：按在招岗位数聚合，返回公司名、行业、城市、在招数与代表岗位 */
    public List<Map<String, Object>> companies(int limit) {
        List<Job> jobs = jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getStatus, 1)
                .orderByDesc(Job::getCreateTime)
                .orderByDesc(Job::getId));
        Map<String, List<Job>> grouped = new LinkedHashMap<>();
        for (Job job : jobs) {
            if (!StringUtils.hasText(job.getCompany())) {
                continue;
            }
            grouped.computeIfAbsent(job.getCompany(), k -> new ArrayList<>()).add(job);
        }
        return grouped.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(limit <= 0 ? 9 : limit)
                .map(e -> {
                    List<Job> list = e.getValue();
                    Job first = list.get(0);
                    // 公司行业取其在招岗位中最多的行业（避免被个别职能岗带偏）
                    String category = list.stream()
                            .map(Job::getCategory)
                            .filter(StringUtils::hasText)
                            .collect(Collectors.groupingBy(x -> x, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(first.getCategory());
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", e.getKey());
                    item.put("category", category);
                    item.put("city", first.getCity());
                    item.put("count", list.size());
                    item.put("jobs", list.stream().limit(3).map(j -> {
                        Map<String, Object> brief = new LinkedHashMap<>();
                        brief.put("id", j.getId());
                        brief.put("title", j.getTitle());
                        brief.put("salaryMin", j.getSalaryMin());
                        brief.put("salaryMax", j.getSalaryMax());
                        return brief;
                    }).collect(Collectors.toList()));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toCountItems(Map<String, Long> count) {
        return count.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public List<Job> hot() {
        return jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getStatus, 1)
                .orderByDesc(Job::getCreateTime)
                .last("LIMIT 6"));
    }

    public Job detail(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BizException(ResultCode.JOB_NOT_FOUND);
        }
        return job;
    }

    public Job publish(Long hrId, String hrName, Job job) {
        job.setId(null);
        job.setHrId(hrId);
        job.setHrName(hrName);
        job.setStatus(1);
        if (!StringUtils.hasText(job.getCompany())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "公司名称不能为空");
        }
        jobMapper.insert(job);
        return job;
    }

    public List<Job> my(Long hrId) {
        return jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getHrId, hrId)
                .orderByDesc(Job::getCreateTime));
    }

    public void offline(Long hrId, Long id) {
        Job job = detail(id);
        if (!hrId.equals(job.getHrId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        job.setStatus(0);
        jobMapper.updateById(job);
    }

    /** 内部接口：供智能匹配服务获取全部在招岗位（DTO 形式） */
    public List<com.recruit.common.dto.JobDTO> allForMatch() {
        List<Job> jobs = jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getStatus, 1)
                .orderByDesc(Job::getCreateTime));
        List<com.recruit.common.dto.JobDTO> result = new ArrayList<>();
        for (Job job : jobs) {
            com.recruit.common.dto.JobDTO dto = new com.recruit.common.dto.JobDTO();
            BeanUtils.copyProperties(job, dto);
            result.add(dto);
        }
        return result;
    }

    public com.recruit.common.dto.JobDTO detailForMatch(Long id) {
        com.recruit.common.dto.JobDTO dto = new com.recruit.common.dto.JobDTO();
        BeanUtils.copyProperties(detail(id), dto);
        return dto;
    }
}

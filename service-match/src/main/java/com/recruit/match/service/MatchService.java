package com.recruit.match.service;

import com.recruit.common.api.Result;
import com.recruit.common.api.ResultCode;
import com.recruit.common.dto.JobDTO;
import com.recruit.common.dto.ResumeDTO;
import com.recruit.common.exception.BizException;
import com.recruit.common.feign.JobClient;
import com.recruit.common.feign.ResumeClient;
import com.recruit.match.dto.MatchDtos.AssistantResult;
import com.recruit.match.dto.MatchDtos.MatchItem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 智能匹配：
 * 1. /match/jobs   —— 根据当前用户简历，对全部在招岗位打分排序（技能/城市/薪资/学历）
 * 2. /match/assistant —— 根据自由文本关键词检索岗位并生成推荐话术
 */
@Service
public class MatchService {

    private final JobClient jobClient;
    private final ResumeClient resumeClient;

    public MatchService(JobClient jobClient, ResumeClient resumeClient) {
        this.jobClient = jobClient;
        this.resumeClient = resumeClient;
    }

    public List<MatchItem> matchJobs(Long userId) {
        Result<ResumeDTO> rr = resumeClient.byUser(userId);
        if (rr == null || rr.getCode() != 200 || rr.getData() == null) {
            throw new BizException(ResultCode.RESUME_NOT_FOUND);
        }
        ResumeDTO resume = rr.getData();
        List<JobDTO> jobs = fetchAllJobs();

        List<MatchItem> items = new ArrayList<>();
        for (JobDTO job : jobs) {
            items.add(score(job, resume));
        }
        items.sort(Comparator.comparingInt(MatchItem::getScore).reversed());
        return items;
    }

    public AssistantResult assistant(String query) {
        List<JobDTO> jobs = fetchAllJobs();
        List<String> tokens = tokenize(query);

        List<JobDTO> hit = new ArrayList<>();
        for (JobDTO job : jobs) {
            String text = (job.getTitle() + " " + safe(job.getTags()) + " " + job.getCompany() + " "
                    + safe(job.getDescription()) + " " + safe(job.getRequirement()) + " " + job.getCity()).toLowerCase();
            for (String t : tokens) {
                if (t.length() >= 2 && text.contains(t)) {
                    hit.add(job);
                    break;
                }
            }
        }
        if (hit.isEmpty()) {
            hit = jobs.stream().limit(6).collect(Collectors.toList());
        }

        AssistantResult result = new AssistantResult();
        result.setJobs(hit.stream().limit(8).collect(Collectors.toList()));
        if (hit.isEmpty()) {
            result.setReply("暂时没有找到匹配的岗位，可以换个关键词试试，例如：Java、前端、产品经理。");
        } else if (tokens.isEmpty()) {
            result.setReply("已为你推荐 " + result.getJobs().size() + " 个最新在招岗位，可结合技能关键词进一步筛选。");
        } else {
            result.setReply("根据你提到的「" + String.join("、", tokens.stream().limit(4).collect(Collectors.toList()))
                    + "」，为你筛选出 " + result.getJobs().size() + " 个匹配岗位，建议优先投递技能匹配度高的职位。");
        }
        return result;
    }

    /** 打分规则：技能命中为主，其次城市、薪资、学历 */
    private MatchItem score(JobDTO job, ResumeDTO resume) {
        List<String> reasons = new ArrayList<>();
        int score = 30;

        Set<String> mySkills = splitLower(resume.getSkills());
        Set<String> jobTags = splitLower(job.getTags());
        List<String> matched = jobTags.stream().filter(mySkills::contains).collect(Collectors.toList());
        if (!matched.isEmpty()) {
            score += Math.min(40, matched.size() * 15);
            reasons.add("技能匹配 " + matched.size() + " 项：" + String.join("/", matched));
        }
        if (resume.getCity() != null && resume.getCity().equals(job.getCity())) {
            score += 15;
            reasons.add("工作城市一致（" + job.getCity() + "）");
        }
        if (resume.getSalary() != null && resume.getSalary() >= job.getSalaryMin()) {
            score += 10;
            reasons.add("期望薪资在岗位区间内（" + job.getSalaryMin() + "-" + job.getSalaryMax() + "K）");
        }
        if (educationRank(resume.getEducation()) >= educationRank(job.getEducation())) {
            score += 5;
            reasons.add("学历满足要求（" + job.getEducation() + "）");
        }
        if (reasons.isEmpty()) {
            reasons.add("基础推荐：岗位仍在招聘中");
        }
        score = Math.min(99, score);

        MatchItem item = new MatchItem();
        item.setJob(job);
        item.setScore(score);
        item.setReasons(reasons);
        return item;
    }

    private List<JobDTO> fetchAllJobs() {
        Result<List<JobDTO>> r = jobClient.listAll();
        if (r == null || r.getCode() != 200 || r.getData() == null) {
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "获取岗位列表失败");
        }
        return r.getData();
    }

    private List<String> tokenize(String query) {
        List<String> tokens = new ArrayList<>();
        if (!StringUtils.hasText(query)) {
            return tokens;
        }
        for (String s : query.toLowerCase().split("[\\s,，。；;、/]+")) {
            if (StringUtils.hasText(s) && !tokens.contains(s)) {
                tokens.add(s.trim());
            }
        }
        return tokens;
    }

    private Set<String> splitLower(String s) {
        if (!StringUtils.hasText(s)) {
            return java.util.Collections.emptySet();
        }
        return Arrays.stream(s.split("[,，、\\s]+"))
                .filter(StringUtils::hasText)
                .map(x -> x.trim().toLowerCase())
                .collect(Collectors.toSet());
    }

    private int educationRank(String edu) {
        if (edu == null) {
            return 1;
        }
        if (edu.contains("博士")) {
            return 4;
        }
        if (edu.contains("硕士")) {
            return 3;
        }
        if (edu.contains("本科")) {
            return 2;
        }
        return 1;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

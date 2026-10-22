package com.recruit.match.dto;

import com.recruit.common.dto.JobDTO;
import lombok.Data;

import java.util.List;

/**
 * 匹配服务 DTO
 */
public class MatchDtos {

    @Data
    public static class MatchItem {
        private JobDTO job;
        private int score;
        private List<String> reasons;
    }

    @Data
    public static class AssistantResult {
        private String reply;
        private List<JobDTO> jobs;
    }
}

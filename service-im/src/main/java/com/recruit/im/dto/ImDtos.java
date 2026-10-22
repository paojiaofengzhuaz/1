package com.recruit.im.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 沟通服务 DTO
 */
public class ImDtos {

    @Data
    public static class StartRequest {
        @NotNull(message = "对方ID不能为空")
        private Long toId;
        private Long jobId;
        private String jobTitle;
    }

    @Data
    public static class SendRequest {
        @NotNull(message = "对方ID不能为空")
        private Long toId;
        private Long convId;
        @NotBlank(message = "消息内容不能为空")
        private String content;
        private Long jobId;
        private String jobTitle;
    }
}

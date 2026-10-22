package com.recruit.match.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 求职小助手 DTO
 */
public class AiDtos {

    /** 一条对话消息（role: system/user/assistant） */
    @Data
    public static class AiMessage {
        private String role;
        private String content;
    }

    /** 对话请求：前端带上最近若干轮历史，实现多轮咨询 */
    @Data
    public static class AiChatRequest {
        private List<AiMessage> messages;
    }

    /** 对话响应 */
    @Data
    public static class AiChatResponse {
        /** 回复内容 */
        private String reply;
        /** 是否由大模型生成（false 表示大模型不可用，已降级为本地规则建议） */
        private boolean ai;
    }

    /** 附件上传解析结果 */
    @Data
    public static class UploadResponse {
        private String fileName;
        private String fileType;
        private long size;
        /** 提取到的正文字符数 */
        private int chars;
        /** 是否因超长被截断 */
        private boolean truncated;
        /** 提取出的正文 */
        private String text;
    }

    /** 附件修改请求 */
    @Data
    public static class RewriteRequest {
        private String fileName;
        /** 原文（从前端上传解析结果回传） */
        private String content;
        /** 修改要求，可为空 */
        private String instruction;
    }

    /** 附件修改结果 */
    @Data
    public static class RewriteResponse {
        /** 修改后的全文 */
        private String content;
        /** 修改说明 */
        private String notes;
        /** 是否由大模型生成 */
        private boolean ai;
        /** 建议下载文件名 */
        private String fileName;
    }
}

package com.recruit.match.controller;

import com.recruit.common.api.Result;
import com.recruit.common.constant.CommonConstants;
import com.recruit.match.dto.AiDtos.AiChatRequest;
import com.recruit.match.dto.AiDtos.AiChatResponse;
import com.recruit.match.dto.AiDtos.RewriteRequest;
import com.recruit.match.dto.AiDtos.RewriteResponse;
import com.recruit.match.dto.AiDtos.UploadResponse;
import com.recruit.match.service.AiService;
import com.recruit.match.util.FileTextExtractor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 求职小助手接口（免登录可用；登录后带上简历上下文，回答更个性化）
 */
@RestController
@RequestMapping("/match/ai")
public class AiController {

    private final AiService aiService;
    private final FileTextExtractor fileTextExtractor;

    public AiController(AiService aiService, FileTextExtractor fileTextExtractor) {
        this.aiService = aiService;
        this.fileTextExtractor = fileTextExtractor;
    }

    /** 多轮求职咨询 */
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(
            @RequestBody(required = false) AiChatRequest request,
            @RequestHeader(value = CommonConstants.USER_ID_HEADER, required = false) Long userId) {
        return Result.ok(aiService.chat(request, userId));
    }

    /** 上传附件：解析为纯文本（支持 txt/md/pdf/docx 等） */
    @PostMapping("/upload")
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileTextExtractor.extract(file));
    }

    /** 修改附件：按修改要求生成修改稿 + 修改说明 */
    @PostMapping("/rewrite")
    public Result<RewriteResponse> rewrite(@RequestBody RewriteRequest request) {
        return Result.ok(aiService.rewrite(request));
    }
}

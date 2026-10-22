package com.recruit.match.util;

import com.recruit.common.api.ResultCode;
import com.recruit.common.exception.BizException;
import com.recruit.match.dto.AiDtos.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 附件文本提取：支持纯文本类（txt/md/json/csv 等）、PDF、Word(.docx)。
 * 图片与 .doc 暂不支持，会返回明确的提示。
 */
@Slf4j
@Component
public class FileTextExtractor {

    /** 单次最多返回的字符数，避免超长文档拖垮模型上下文 */
    private static final int MAX_CHARS = 12000;

    private static final Set<String> TEXT_EXT = new HashSet<>(Arrays.asList(
            "txt", "md", "markdown", "json", "csv", "log", "xml", "yml", "yaml",
            "html", "htm", "java", "js", "ts", "vue", "css", "sql", "properties"));

    public UploadResponse extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "请选择要上传的附件");
        }
        String name = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "attachment";
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "附件不能超过 10MB");
        }
        String ext = ext(name);

        String text;
        try {
            byte[] bytes = file.getBytes();
            switch (ext) {
                case "pdf":
                    text = fromPdf(bytes);
                    break;
                case "docx":
                    text = fromDocx(bytes);
                    break;
                case "doc":
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            "暂不支持旧版 .doc，请用 Word 另存为 .docx 后再上传");
                case "png":
                case "jpg":
                case "jpeg":
                case "gif":
                case "webp":
                case "bmp":
                    throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                            "暂不支持图片识别，请上传文本、PDF 或 Word 文件");
                default:
                    if (TEXT_EXT.contains(ext) || ext.isEmpty()) {
                        text = decodeText(bytes);
                    } else {
                        // 未知扩展名：先按文本尝试，失败给出提示
                        text = decodeText(bytes);
                        if (!StringUtils.hasText(text)) {
                            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                                    "无法识别该文件类型（支持 txt/md/pdf/docx）");
                        }
                    }
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("附件解析失败: {}", name, e);
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "附件解析失败，请确认文件未损坏");
        }

        text = text.replace("\r\n", "\n").trim();
        if (!StringUtils.hasText(text)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "附件中没有识别到文本内容");
        }

        UploadResponse resp = new UploadResponse();
        resp.setFileName(name);
        resp.setFileType(ext.isEmpty() ? "text" : ext);
        resp.setSize(file.getSize());
        resp.setChars(text.length());
        if (text.length() > MAX_CHARS) {
            resp.setText(text.substring(0, MAX_CHARS));
            resp.setTruncated(true);
        } else {
            resp.setText(text);
            resp.setTruncated(false);
        }
        return resp;
    }

    private String fromPdf(byte[] bytes) throws Exception {
        try (PDDocument doc = PDDocument.load(bytes)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String fromDocx(byte[] bytes) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                if (StringUtils.hasText(p.getText())) {
                    sb.append(p.getText()).append('\n');
                }
            }
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        sb.append(cell.getText()).append(" | ");
                    }
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    /** UTF-8 严格解码，失败回退 GBK（兼容 Windows 记事本保存的中文文件） */
    private String decodeText(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("GBK"));
        }
    }

    private String ext(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase();
    }
}

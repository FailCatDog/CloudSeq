package cn.guet.soft_manage.biz.export.controller;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContentRequestDTO;
import cn.guet.soft_manage.biz.export.service.ExportService;
import jakarta.annotation.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 节点导出（同步直传）
 */
@RestController
@RequestMapping("/api/nodes")
public class ExportController {

    @Resource
    private ExportService exportService;

    @GetMapping("/{nodeId}/export")
    public ResponseEntity<byte[]> export(
            @PathVariable Long nodeId,
            @RequestParam String format) {
        return toResponse(exportService.export(nodeId, format));
    }

    /**
     * 携带编辑器当前 contentMd/快照导出，避免协同 debounce 导致库内正文为空。
     */
    @PostMapping("/{nodeId}/export")
    public ResponseEntity<byte[]> exportWithContent(
            @PathVariable Long nodeId,
            @RequestParam String format,
            @RequestBody(required = false) ExportContentRequestDTO body) {
        String override = body != null ? body.getContentMd() : null;
        return toResponse(exportService.export(nodeId, format, override));
    }

    private static ResponseEntity<byte[]> toResponse(ExportArtifact artifact) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(artifact.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(artifact.getContentType()))
                .body(artifact.getBytes());
    }
}

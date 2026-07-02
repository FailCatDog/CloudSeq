package cn.guet.soft_manage.biz.document.util;

import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import cn.guet.soft_manage.frame.constant.DocumentConstants;

import java.nio.charset.StandardCharsets;

/**
 * 文档正文元数据计算
 */
public final class WorkspaceContentMetadataUtil {

    private WorkspaceContentMetadataUtil() {
    }

    public static void applyTo(WorkspaceContent content) {
        if (content == null) {
            return;
        }
        String contentMd = content.getContentMd() != null ? content.getContentMd() : "";
        byte[] yjsState = content.getYjsState();

        content.setCharCount(countChars(contentMd));
        content.setContentBytes(contentMd.getBytes(StandardCharsets.UTF_8).length);
        content.setYjsBytes(yjsState != null ? yjsState.length : 0);
        content.setSummary(buildSummary(contentMd));
    }

    public static int countChars(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.codePointCount(0, text.length());
    }

    public static String buildSummary(String contentMd) {
        if (contentMd == null || contentMd.isBlank()) {
            return "";
        }
        String normalized = contentMd
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return "";
        }
        int max = DocumentConstants.SUMMARY_MAX_LENGTH;
        if (normalized.codePointCount(0, normalized.length()) <= max) {
            return normalized;
        }
        int endIndex = normalized.offsetByCodePoints(0, max);
        return normalized.substring(0, endIndex);
    }
}

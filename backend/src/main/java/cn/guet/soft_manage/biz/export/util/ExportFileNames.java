package cn.guet.soft_manage.biz.export.util;

import java.util.regex.Pattern;

/**
 * 导出文件名构建与消毒
 */
public final class ExportFileNames {

    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    private ExportFileNames() {
    }

    public static String build(String title, String extension) {
        String base = title == null ? "" : title.trim();
        if (!base.isEmpty()) {
            String[] segments = base.split("[/\\\\]");
            base = segments[segments.length - 1].trim();
            base = ILLEGAL_CHARS.matcher(base).replaceAll("_");
        }
        if (base.isBlank()) {
            base = "node";
        }
        return base + "." + extension;
    }
}

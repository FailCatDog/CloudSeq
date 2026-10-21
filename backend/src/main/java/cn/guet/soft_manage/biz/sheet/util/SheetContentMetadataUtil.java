package cn.guet.soft_manage.biz.sheet.util;

import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import cn.guet.soft_manage.frame.constant.DocumentConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/**
 * 表格正文元数据计算
 */
public final class SheetContentMetadataUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SheetContentMetadataUtil() {
    }

    public static void applyTo(WorkspaceContent content) {
        if (content == null) {
            return;
        }
        String contentJson = content.getContentMd() != null ? content.getContentMd() : "";
        byte[] yjsState = content.getYjsState();

        content.setCharCount(countCells(contentJson));
        content.setContentBytes(contentJson.getBytes(StandardCharsets.UTF_8).length);
        content.setYjsBytes(yjsState != null ? yjsState.length : 0);
        content.setSummary(buildSummary(contentJson));
    }

    public static int countCells(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return 0;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(contentJson);
            JsonNode sheets = root.get("sheets");
            if (sheets == null || !sheets.isArray()) {
                return 0;
            }
            int total = 0;
            for (JsonNode sheet : sheets) {
                JsonNode cells = sheet.get("cells");
                if (cells != null && cells.isObject()) {
                    total += cells.size();
                }
            }
            return total;
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static String buildSummary(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return "空表格";
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(contentJson);
            JsonNode sheets = root.get("sheets");
            if (sheets == null || !sheets.isArray() || sheets.isEmpty()) {
                return "空表格";
            }
            int sheetCount = sheets.size();
            int cellCount = countCells(contentJson);
            String summary = sheetCount + " 个工作表 · " + cellCount + " 个单元格";
            int max = DocumentConstants.SUMMARY_MAX_LENGTH;
            if (summary.codePointCount(0, summary.length()) <= max) {
                return summary;
            }
            int endIndex = summary.offsetByCodePoints(0, max);
            return summary.substring(0, endIndex);
        } catch (Exception ignored) {
            return "表格";
        }
    }
}

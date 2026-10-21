package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.util.ExportFileNames;
import cn.guet.soft_manage.frame.config.ExportProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Exports SHEET nodes to XLSX via Apache POI XSSF.
 * Primary input: SpeedSheet {@code WorkbookSnapshot} JSON ({@code sheets[].cells} keyed by {@code rowId:colId}).
 * Fallback: Luckysheet-like {@code celldata: [{r,c,v}]}.
 */
@Component
public class XlsxNodeExporter implements NodeExporter {

    private final ExportProperties properties;
    private final ObjectMapper objectMapper;

    public XlsxNodeExporter(ExportProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String nodeType, ExportFormat format) {
        return "SHEET".equals(nodeType) && format == ExportFormat.XLSX;
    }

    @Override
    public ExportArtifact export(ExportContext ctx) {
        String contentMd = ctx.getContentMd() == null ? "" : ctx.getContentMd();
        try {
            JsonNode root = contentMd.isBlank()
                ? objectMapper.createObjectNode()
                : objectMapper.readTree(contentMd);
            JsonNode sheets = root.get("sheets");
            if (sheets == null || !sheets.isArray()) {
                sheets = objectMapper.createArrayNode();
            }

            int cellCount = countNonEmptyCells(sheets);
            if (cellCount > properties.getMaxSheetCells()) {
                throw new BusinessException(BizResponseCode.EXPORT_CONTENT_TOO_LARGE);
            }

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                while (workbook.getNumberOfSheets() > 0) {
                    workbook.removeSheetAt(0);
                }

                if (sheets.isEmpty()) {
                    workbook.createSheet(uniqueSheetName(workbook, "Sheet1"));
                } else {
                    for (int i = 0; i < sheets.size(); i++) {
                        writeSheet(workbook, sheets.get(i), i);
                    }
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return ExportArtifact.builder()
                    .bytes(out.toByteArray())
                    .contentType(ExportFormat.XLSX.getContentType())
                    .fileName(ExportFileNames.build(ctx.getTitle(), "xlsx"))
                    .build();
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.EXPORT_RENDER_FAILED);
        }
    }

    private void writeSheet(Workbook workbook, JsonNode sheetNode, int sheetIndex) {
        String rawName = textOrEmpty(sheetNode.get("name"));
        if (rawName.isBlank()) {
            rawName = "Sheet" + (sheetIndex + 1);
        }
        org.apache.poi.ss.usermodel.Sheet poiSheet =
            workbook.createSheet(uniqueSheetName(workbook, rawName));

        JsonNode cells = sheetNode.get("cells");
        if (cells != null && cells.isObject() && cells.size() > 0) {
            writeSpeedSheetCells(poiSheet, sheetNode, cells);
            return;
        }

        JsonNode celldata = sheetNode.get("celldata");
        if (celldata != null && celldata.isArray()) {
            writeLuckysheetCelldata(poiSheet, celldata);
        }
    }

    private static void writeSpeedSheetCells(
        org.apache.poi.ss.usermodel.Sheet poiSheet,
        JsonNode sheetNode,
        JsonNode cells
    ) {
        Map<String, Integer> rowIndex = orderIndex(sheetNode.get("rowOrder"));
        Map<String, Integer> colIndex = orderIndex(sheetNode.get("colOrder"));

        Iterator<Map.Entry<String, JsonNode>> fields = cells.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String display = displayValue(entry.getValue());
            if (display == null) {
                continue;
            }
            int[] rc = resolveSpeedSheetPosition(entry.getKey(), rowIndex, colIndex);
            if (rc == null) {
                continue;
            }
            setCellString(poiSheet, rc[0], rc[1], display);
        }
    }

    private static void writeLuckysheetCelldata(
        org.apache.poi.ss.usermodel.Sheet poiSheet,
        JsonNode celldata
    ) {
        for (JsonNode item : celldata) {
            if (item == null || !item.isObject()) {
                continue;
            }
            JsonNode rNode = item.get("r");
            JsonNode cNode = item.get("c");
            if (rNode == null || cNode == null || !rNode.canConvertToInt() || !cNode.canConvertToInt()) {
                continue;
            }
            String display = displayValue(item.get("v"));
            if (display == null) {
                continue;
            }
            setCellString(poiSheet, rNode.asInt(), cNode.asInt(), display);
        }
    }

    /**
     * Map {@code rowId:colId} to grid indices via rowOrder/colOrder.
     */
    private static int[] resolveSpeedSheetPosition(
        String key,
        Map<String, Integer> rowIndex,
        Map<String, Integer> colIndex
    ) {
        if (key == null || key.isBlank()) {
            return null;
        }
        int sep = key.indexOf(':');
        if (sep <= 0 || sep >= key.length() - 1) {
            return null;
        }
        String rowId = key.substring(0, sep);
        String colId = key.substring(sep + 1);
        Integer r = rowIndex.get(rowId);
        Integer c = colIndex.get(colId);
        if (r == null || c == null) {
            return null;
        }
        return new int[]{r, c};
    }

    private static Map<String, Integer> orderIndex(JsonNode order) {
        Map<String, Integer> index = new HashMap<>();
        if (order == null || !order.isArray()) {
            return index;
        }
        for (int i = 0; i < order.size(); i++) {
            String id = textOrEmpty(order.get(i));
            if (!id.isBlank()) {
                index.putIfAbsent(id, i);
            }
        }
        return index;
    }

    private static int countNonEmptyCells(JsonNode sheets) {
        int total = 0;
        for (JsonNode sheet : sheets) {
            JsonNode cells = sheet.get("cells");
            if (cells != null && cells.isObject() && cells.size() > 0) {
                Iterator<JsonNode> values = cells.elements();
                while (values.hasNext()) {
                    if (displayValue(values.next()) != null) {
                        total++;
                    }
                }
                continue;
            }
            JsonNode celldata = sheet.get("celldata");
            if (celldata != null && celldata.isArray()) {
                for (JsonNode item : celldata) {
                    if (item != null && item.isObject() && displayValue(item.get("v")) != null) {
                        total++;
                    }
                }
            }
        }
        return total;
    }

    /**
     * Prefer formatted display {@code m}, else stringify {@code v}.
     */
    private static String displayValue(JsonNode cell) {
        if (cell == null || cell.isNull()) {
            return null;
        }
        if (cell.isValueNode()) {
            String text = cell.asText();
            return text == null || text.isBlank() ? null : text;
        }
        if (!cell.isObject()) {
            return null;
        }
        JsonNode m = cell.get("m");
        if (m != null && !m.isNull()) {
            String formatted = m.asText();
            if (formatted != null && !formatted.isBlank()) {
                return formatted;
            }
        }
        JsonNode v = cell.get("v");
        if (v == null || v.isNull()) {
            return null;
        }
        String raw = v.asText();
        return raw == null || raw.isBlank() ? null : raw;
    }

    private static void setCellString(
        org.apache.poi.ss.usermodel.Sheet sheet,
        int rowIdx,
        int colIdx,
        String value
    ) {
        if (rowIdx < 0 || colIdx < 0) {
            return;
        }
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }
        cell.setCellValue(value);
    }

    private static String uniqueSheetName(Workbook workbook, String preferred) {
        String safe = WorkbookUtil.createSafeSheetName(preferred == null || preferred.isBlank() ? "Sheet" : preferred);
        if (workbook.getSheet(safe) == null) {
            return safe;
        }
        Set<String> existing = new HashSet<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            existing.add(workbook.getSheetName(i));
        }
        for (int n = 2; n < 10_000; n++) {
            String suffix = " (" + n + ")";
            String base = safe;
            int maxBase = Math.max(1, 31 - suffix.length());
            if (base.length() > maxBase) {
                base = base.substring(0, maxBase);
            }
            String candidate = base + suffix;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
        return WorkbookUtil.createSafeSheetName(safe + System.nanoTime());
    }

    private static String textOrEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        String text = node.asText();
        return text == null ? "" : text;
    }
}

package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.frame.config.ExportProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XlsxNodeExporterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DataFormatter FORMATTER = new DataFormatter();

    @Test
    void exportsSpeedSheetSnapshotToXlsx() throws Exception {
        String snapshot = loadFixture("export/sheet-snapshot-sample.json");
        XlsxNodeExporter exporter = new XlsxNodeExporter(new ExportProperties(), MAPPER);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("SHEET")
            .title("季度表")
            .contentMd(snapshot)
            .build();

        ExportArtifact art = exporter.export(ctx);

        assertTrue(art.getBytes().length > 100);
        assertEquals('P', art.getBytes()[0]);
        assertEquals('K', art.getBytes()[1]);
        assertTrue(art.getFileName().endsWith(".xlsx"));
        assertEquals(ExportFormat.XLSX.getContentType(), art.getContentType());
        assertTrue(exporter.supports("SHEET", ExportFormat.XLSX));
        assertFalse(exporter.supports("DOCUMENT", ExportFormat.XLSX));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(art.getBytes()))) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("销售明细", workbook.getSheetName(0));
            assertEquals("Sheet2", workbook.getSheetName(1));

            Sheet sales = workbook.getSheetAt(0);
            assertEquals("产品", cellText(sales, 0, 0));
            assertEquals("数量", cellText(sales, 0, 1));
            assertEquals("金额", cellText(sales, 0, 2));
            assertEquals("Widget", cellText(sales, 1, 0));
            assertEquals("12", cellText(sales, 1, 1));
            assertEquals("1,200.50", cellText(sales, 1, 2));
            assertEquals("Gadget", cellText(sales, 2, 0));

            Sheet notes = workbook.getSheetAt(1);
            assertEquals("备注", cellText(notes, 0, 0));
            assertEquals("TRUE", cellText(notes, 1, 1));
        }
    }

    @Test
    void rejectsOversizedSheetCellCount() throws Exception {
        String snapshot = loadFixture("export/sheet-snapshot-sample.json");
        ExportProperties props = new ExportProperties();
        props.setMaxSheetCells(3);
        XlsxNodeExporter exporter = new XlsxNodeExporter(props, MAPPER);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("SHEET")
            .title("过大")
            .contentMd(snapshot)
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> exporter.export(ctx));
        assertEquals(BizResponseCode.EXPORT_CONTENT_TOO_LARGE.getCode(), ex.getCode());
    }

    @Test
    void exportsLuckysheetCelldataFallback() throws Exception {
        String luckysheet = """
            {
              "sheets": [
                {
                  "name": "旧表",
                  "celldata": [
                    {"r": 0, "c": 0, "v": {"v": "A1", "m": "A1"}},
                    {"r": 1, "c": 2, "v": {"v": 42, "m": "42"}}
                  ]
                }
              ]
            }
            """;
        XlsxNodeExporter exporter = new XlsxNodeExporter(new ExportProperties(), MAPPER);
        ExportContext ctx = ExportContext.builder()
            .nodeId(2L).workspaceId(1L).nodeType("SHEET")
            .title("兼容")
            .contentMd(luckysheet)
            .build();

        ExportArtifact art = exporter.export(ctx);

        assertEquals('P', art.getBytes()[0]);
        assertEquals('K', art.getBytes()[1]);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(art.getBytes()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("旧表", workbook.getSheetName(0));
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("A1", cellText(sheet, 0, 0));
            assertEquals("42", cellText(sheet, 1, 2));
        }
    }

    private static String loadFixture(String classpath) throws Exception {
        try (InputStream in = XlsxNodeExporterTest.class.getClassLoader().getResourceAsStream(classpath)) {
            if (in == null) {
                throw new IllegalStateException("Missing fixture: " + classpath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String cellText(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return "";
        }
        return FORMATTER.formatCellValue(cell);
    }
}

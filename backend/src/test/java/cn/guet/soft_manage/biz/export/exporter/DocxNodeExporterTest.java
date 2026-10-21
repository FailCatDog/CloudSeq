package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.frame.config.ExportProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocxNodeExporterTest {

    /** Minimal valid 1×1 RGB PNG. */
    private static final byte[] TINY_PNG = new byte[]{
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
        (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
        0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00,
        0x00, 0x00, 0x03, 0x00, 0x01, 0x00, 0x05, (byte) 0xFE,
        (byte) 0xD4, (byte) 0xEF, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
        0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    @Test
    void exportsMinimalDocxWithMagicBytes() {
        ExportAssetResolver resolver = (ws, url) -> null;
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("演示")
            .contentMd("# Hello\n\nWorld")
            .assetResolver(resolver)
            .build();
        ExportArtifact art = exporter.export(ctx);
        assertTrue(art.getBytes().length > 100);
        assertEquals('P', art.getBytes()[0]);
        assertEquals('K', art.getBytes()[1]);
        assertTrue(art.getFileName().endsWith(".docx"));
        assertEquals(ExportFormat.DOCX.getContentType(), art.getContentType());
        assertTrue(exporter.supports("DOCUMENT", ExportFormat.DOCX));
        assertFalse(exporter.supports("SHEET", ExportFormat.DOCX));
    }

    @Test
    void embedsTinyPngWhenResolverReturnsBytes() throws Exception {
        ExportAssetResolver resolver = (ws, url) -> {
            if ("/api/assets/1".equals(url)) {
                return ResolvedExportAsset.builder()
                    .bytes(TINY_PNG)
                    .contentType("image/png")
                    .fileName("tiny.png")
                    .build();
            }
            return null;
        };
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("图")
            .contentMd("![alt](/api/assets/1)")
            .assetResolver(resolver)
            .build();

        ExportArtifact art = exporter.export(ctx);

        assertEquals('P', art.getBytes()[0]);
        assertEquals('K', art.getBytes()[1]);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            assertFalse(doc.getAllPictures().isEmpty());
            assertEquals(TINY_PNG.length, doc.getAllPictures().get(0).getData().length);
        }
    }

    @Test
    void embedsPngFromTipTapHtmlImgWithDimensions() throws Exception {
        ExportAssetResolver resolver = (ws, url) -> {
            if ("/api/assets/7".equals(url)) {
                return ResolvedExportAsset.builder()
                    .bytes(TINY_PNG)
                    .contentType("image/png")
                    .fileName("shot.png")
                    .build();
            }
            return null;
        };
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("HTML图")
            .contentMd("<img class=\"ps-doc-image\" src=\"/api/assets/7\" alt=\"shot.png\" width=\"400\" height=\"300\" />\n")
            .assetResolver(resolver)
            .build();

        ExportArtifact art = exporter.export(ctx);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            assertFalse(doc.getAllPictures().isEmpty());
            assertEquals(TINY_PNG.length, doc.getAllPictures().get(0).getData().length);
        }
    }

    @Test
    void rendersMarkdownBoldWithoutAsterisks() throws Exception {
        ExportAssetResolver resolver = (ws, url) -> null;
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("加粗")
            .contentMd("这是**粗体**文字")
            .assetResolver(resolver)
            .build());

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            String all = doc.getParagraphs().stream().map(p -> p.getText()).reduce("", String::concat);
            assertFalse(all.contains("**"));
            assertTrue(all.contains("粗体"));
            boolean hasBold = doc.getParagraphs().stream()
                .flatMap(p -> p.getRuns().stream())
                .anyMatch(r -> Boolean.TRUE.equals(r.isBold()) && r.text() != null && r.text().contains("粗体"));
            assertTrue(hasBold);
        }
    }

    @Test
    void embedsPngWhenContentTypeMissingUsingMagicBytes() throws Exception {
        ExportAssetResolver resolver = (ws, url) -> ResolvedExportAsset.builder()
            .bytes(TINY_PNG)
            .contentType(null)
            .fileName("x.bin")
            .build();
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("魔数")
            .contentMd("![x](/api/assets/1)")
            .assetResolver(resolver)
            .build());

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            assertFalse(doc.getAllPictures().isEmpty());
        }
    }

    @Test
    void missingImageBecomesPlaceholder() throws Exception {
        ExportAssetResolver resolver = (ws, url) -> null;
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("缺图")
            .contentMd("![gone](/api/assets/99)")
            .assetResolver(resolver)
            .build();

        ExportArtifact art = exporter.export(ctx);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            assertTrue(doc.getAllPictures().isEmpty());
            String text = doc.getParagraphs().stream()
                .map(p -> p.getText())
                .reduce("", String::concat);
            assertTrue(text.contains("[图片不可用]"));
        }
    }

    @Test
    void unsupportedWebpImageBecomesPlaceholder() throws Exception {
        byte[] fakeWebp = new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        ExportAssetResolver resolver = (ws, url) -> ResolvedExportAsset.builder()
            .bytes(fakeWebp)
            .contentType("image/webp")
            .fileName("photo.webp")
            .build();
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("webp")
            .contentMd("![w](/api/assets/2)")
            .assetResolver(resolver)
            .build();

        ExportArtifact art = exporter.export(ctx);

        assertEquals('P', art.getBytes()[0]);
        assertEquals('K', art.getBytes()[1]);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            assertTrue(doc.getAllPictures().isEmpty());
            String text = doc.getParagraphs().stream()
                .map(p -> p.getText())
                .reduce("", String::concat);
            assertTrue(text.contains("[图片不可用]"));
        }
    }

    @Test
    void rejectsOversizedContent() {
        ExportProperties props = new ExportProperties();
        props.setMaxDocumentChars(5);
        ExportAssetResolver resolver = (ws, url) -> null;
        DocxNodeExporter exporter = new DocxNodeExporter(props, resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("大")
            .contentMd("abcdef")
            .assetResolver(resolver)
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> exporter.export(ctx));
        assertEquals(BizResponseCode.EXPORT_CONTENT_TOO_LARGE.getCode(), ex.getCode());
    }

    @Test
    void rejectsOversizedEmbeddedImages() {
        ExportProperties props = new ExportProperties();
        props.setMaxEmbeddedImageBytes(10);
        ExportAssetResolver resolver = (ws, url) -> ResolvedExportAsset.builder()
            .bytes(TINY_PNG)
            .contentType("image/png")
            .fileName("tiny.png")
            .build();
        DocxNodeExporter exporter = new DocxNodeExporter(props, resolver);
        ExportContext ctx = ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("大图")
            .contentMd("![x](/api/assets/1)")
            .assetResolver(resolver)
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> exporter.export(ctx));
        assertEquals(BizResponseCode.EXPORT_IMAGES_TOO_LARGE.getCode(), ex.getCode());
    }

    @Test
    void embedsExternalHttpImageWhenResolverReturnsBytes() throws Exception {
        ExportAssetResolver resolver = (ws, url) -> {
            if ("https://pdai.tech/images/arch_sqlyuanli.png".equals(url)) {
                return ResolvedExportAsset.builder()
                    .bytes(TINY_PNG)
                    .contentType("image/png")
                    .fileName("arch.png")
                    .build();
            }
            return null;
        };
        DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
            .title("外链图")
            .contentMd("<img class=\"ps-doc-image\" src=\"https://pdai.tech/images/arch_sqlyuanli.png\" width=\"780\" height=\"332\" />\n")
            .assetResolver(resolver)
            .build());

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(art.getBytes()))) {
            assertFalse(doc.getAllPictures().isEmpty());
        }
    }
}

package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.frame.config.ExportProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfNodeExporterTest {

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
    void exportsPdfHeader() {
        PdfNodeExporter exporter = new PdfNodeExporter(new ExportProperties(), (w, u) -> null);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .workspaceId(1L).nodeType("DOCUMENT").title("t")
            .contentMd("# Hi\n\nBody")
            .assetResolver((w, u) -> null)
            .build());
        assertTrue(new String(art.getBytes(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
        assertTrue(art.getFileName().endsWith(".pdf"));
        assertEquals(ExportFormat.PDF.getContentType(), art.getContentType());
        assertTrue(exporter.supports("DOCUMENT", ExportFormat.PDF));
        assertFalse(exporter.supports("DOCUMENT", ExportFormat.DOCX));
        assertFalse(exporter.supports("SHEET", ExportFormat.PDF));
    }

    @Test
    void embedsTinyPngAsDataUriWhenResolverReturnsBytes() {
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
        PdfNodeExporter exporter = new PdfNodeExporter(new ExportProperties(), resolver);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .workspaceId(1L).nodeType("DOCUMENT").title("图")
            .contentMd("![alt](/api/assets/1)")
            .assetResolver(resolver)
            .build());

        assertTrue(new String(art.getBytes(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
        assertTrue(art.getBytes().length > 100);
    }

    @Test
    void missingImageDoesNotFailExport() {
        ExportAssetResolver resolver = (ws, url) -> null;
        PdfNodeExporter exporter = new PdfNodeExporter(new ExportProperties(), resolver);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .workspaceId(1L).nodeType("DOCUMENT").title("缺图")
            .contentMd("![gone](/api/assets/99)")
            .assetResolver(resolver)
            .build());

        assertTrue(new String(art.getBytes(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }

    @Test
    void unsupportedWebpImageDoesNotFailExport() {
        byte[] fakeWebp = new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        ExportAssetResolver resolver = (ws, url) -> ResolvedExportAsset.builder()
            .bytes(fakeWebp)
            .contentType("image/webp")
            .fileName("photo.webp")
            .build();
        PdfNodeExporter exporter = new PdfNodeExporter(new ExportProperties(), resolver);
        ExportArtifact art = exporter.export(ExportContext.builder()
            .workspaceId(1L).nodeType("DOCUMENT").title("webp")
            .contentMd("![w](/api/assets/2)")
            .assetResolver(resolver)
            .build());

        assertTrue(new String(art.getBytes(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }

    @Test
    void rejectsOversizedContent() {
        ExportProperties props = new ExportProperties();
        props.setMaxDocumentChars(5);
        PdfNodeExporter exporter = new PdfNodeExporter(props, (w, u) -> null);
        BusinessException ex = assertThrows(BusinessException.class, () ->
            exporter.export(ExportContext.builder()
                .workspaceId(1L).nodeType("DOCUMENT").title("大")
                .contentMd("abcdef")
                .assetResolver((w, u) -> null)
                .build()));
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
        PdfNodeExporter exporter = new PdfNodeExporter(props, resolver);
        BusinessException ex = assertThrows(BusinessException.class, () ->
            exporter.export(ExportContext.builder()
                .workspaceId(1L).nodeType("DOCUMENT").title("大图")
                .contentMd("![x](/api/assets/1)")
                .assetResolver(resolver)
                .build()));
        assertEquals(BizResponseCode.EXPORT_IMAGES_TOO_LARGE.getCode(), ex.getCode());
    }
}

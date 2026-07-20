package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.biz.export.markdown.MarkdownBlock;
import cn.guet.soft_manage.biz.export.markdown.MarkdownDocumentParser;
import cn.guet.soft_manage.biz.export.util.ExportFileNames;
import cn.guet.soft_manage.frame.config.ExportProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Exports DOCUMENT nodes to DOCX via Apache POI XWPF.
 */
@Component
public class DocxNodeExporter implements NodeExporter {

    private static final String MISSING_IMAGE_PLACEHOLDER = "[图片不可用]";
    private static final int DEFAULT_IMAGE_WIDTH_PX = 400;
    private static final int DEFAULT_IMAGE_HEIGHT_PX = 300;

    private final ExportProperties properties;
    private final ExportAssetResolver assetResolver;

    public DocxNodeExporter(ExportProperties properties, ExportAssetResolver assetResolver) {
        this.properties = properties;
        this.assetResolver = assetResolver;
    }

    @Override
    public boolean supports(String nodeType, ExportFormat format) {
        return "DOCUMENT".equals(nodeType) && format == ExportFormat.DOCX;
    }

    @Override
    public ExportArtifact export(ExportContext ctx) {
        String contentMd = ctx.getContentMd() == null ? "" : ctx.getContentMd();
        if (contentMd.codePointCount(0, contentMd.length()) > properties.getMaxDocumentChars()) {
            throw new BusinessException(BizResponseCode.EXPORT_CONTENT_TOO_LARGE);
        }

        ExportAssetResolver resolver = ctx.getAssetResolver() != null ? ctx.getAssetResolver() : assetResolver;
        List<MarkdownBlock> blocks = MarkdownDocumentParser.parse(contentMd);

        try (XWPFDocument document = new XWPFDocument()) {
            long embeddedBytes = 0;
            int orderedIndex = 0;
            for (MarkdownBlock block : blocks) {
                switch (block.getType()) {
                    case HEADING -> appendHeading(document, block);
                    case PARAGRAPH -> appendParagraph(document, nullToEmpty(block.getText()));
                    case BULLET_ITEM -> {
                        orderedIndex = 0;
                        appendParagraph(document, "• " + nullToEmpty(block.getText()));
                    }
                    case ORDERED_ITEM -> {
                        orderedIndex++;
                        appendParagraph(document, orderedIndex + ". " + nullToEmpty(block.getText()));
                    }
                    case TABLE -> appendTable(document, block);
                    case IMAGE -> embeddedBytes = appendImage(document, ctx.getWorkspaceId(), block, resolver, embeddedBytes);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return ExportArtifact.builder()
                .bytes(out.toByteArray())
                .contentType(ExportFormat.DOCX.getContentType())
                .fileName(ExportFileNames.build(ctx.getTitle(), "docx"))
                .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.EXPORT_RENDER_FAILED);
        }
    }

    private static void appendHeading(XWPFDocument document, MarkdownBlock block) {
        XWPFParagraph paragraph = document.createParagraph();
        int level = block.getLevel() == null ? 1 : Math.min(6, Math.max(1, block.getLevel()));
        paragraph.setStyle("Heading" + level);
        XWPFRun run = paragraph.createRun();
        run.setText(nullToEmpty(block.getText()));
        run.setBold(true);
        run.setFontSize(Math.max(12, 24 - (level - 1) * 2));
    }

    private static void appendParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.createRun().setText(text);
    }

    private static void appendTable(XWPFDocument document, MarkdownBlock block) {
        List<List<String>> rows = block.getTableRows();
        if (rows == null || rows.isEmpty()) {
            return;
        }
        int cols = rows.stream().mapToInt(List::size).max().orElse(0);
        if (cols == 0) {
            return;
        }
        XWPFTable table = document.createTable(rows.size(), cols);
        for (int r = 0; r < rows.size(); r++) {
            XWPFTableRow row = table.getRow(r);
            List<String> cells = rows.get(r);
            for (int c = 0; c < cols; c++) {
                XWPFTableCell cell = row.getCell(c);
                String value = c < cells.size() ? nullToEmpty(cells.get(c)) : "";
                cell.setText(value);
            }
        }
    }

    private long appendImage(
        XWPFDocument document,
        Long workspaceId,
        MarkdownBlock block,
        ExportAssetResolver resolver,
        long embeddedBytes
    ) throws Exception {
        ResolvedExportAsset asset = resolver == null ? null : resolver.resolve(workspaceId, block.getUrl());
        if (asset == null || asset.getBytes() == null || asset.getBytes().length == 0) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setItalic(true);
            run.setText(MISSING_IMAGE_PLACEHOLDER);
            return embeddedBytes;
        }

        long next = embeddedBytes + asset.getBytes().length;
        if (next > properties.getMaxEmbeddedImageBytes()) {
            throw new BusinessException(BizResponseCode.EXPORT_IMAGES_TOO_LARGE);
        }

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        int pictureType = pictureType(asset.getContentType());
        String fileName = asset.getFileName() != null && !asset.getFileName().isBlank()
            ? asset.getFileName()
            : "image";
        try (ByteArrayInputStream in = new ByteArrayInputStream(asset.getBytes())) {
            run.addPicture(
                in,
                pictureType,
                fileName,
                Units.toEMU(DEFAULT_IMAGE_WIDTH_PX),
                Units.toEMU(DEFAULT_IMAGE_HEIGHT_PX)
            );
        }
        return next;
    }

    private static int pictureType(String contentType) {
        if (contentType == null) {
            return XWPFDocument.PICTURE_TYPE_PNG;
        }
        String ct = contentType.toLowerCase();
        if (ct.contains("jpeg") || ct.contains("jpg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        }
        if (ct.contains("gif")) {
            return XWPFDocument.PICTURE_TYPE_GIF;
        }
        if (ct.contains("bmp")) {
            return XWPFDocument.PICTURE_TYPE_BMP;
        }
        return XWPFDocument.PICTURE_TYPE_PNG;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

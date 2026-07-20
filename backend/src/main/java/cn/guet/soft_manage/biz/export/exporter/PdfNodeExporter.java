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
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

/**
 * Exports DOCUMENT nodes to PDF via Markdown → HTML → openhtmltopdf.
 */
@Component
public class PdfNodeExporter implements NodeExporter {

    private static final String MISSING_IMAGE_PLACEHOLDER = "[图片不可用]";
    private static final String FONT_RESOURCE = "fonts/NotoSansSC-Regular.otf";
    private static final String FONT_FAMILY = "Noto Sans SC";

    private final ExportProperties properties;
    private final ExportAssetResolver assetResolver;

    public PdfNodeExporter(ExportProperties properties, ExportAssetResolver assetResolver) {
        this.properties = properties;
        this.assetResolver = assetResolver;
    }

    @Override
    public boolean supports(String nodeType, ExportFormat format) {
        return "DOCUMENT".equals(nodeType) && format == ExportFormat.PDF;
    }

    @Override
    public ExportArtifact export(ExportContext ctx) {
        String contentMd = ctx.getContentMd() == null ? "" : ctx.getContentMd();
        if (contentMd.codePointCount(0, contentMd.length()) > properties.getMaxDocumentChars()) {
            throw new BusinessException(BizResponseCode.EXPORT_CONTENT_TOO_LARGE);
        }

        ExportAssetResolver resolver = ctx.getAssetResolver() != null ? ctx.getAssetResolver() : assetResolver;
        List<MarkdownBlock> blocks = MarkdownDocumentParser.parse(contentMd);

        try {
            StringBuilder body = new StringBuilder();
            long embeddedBytes = 0;
            int orderedIndex = 0;
            for (MarkdownBlock block : blocks) {
                switch (block.getType()) {
                    case HEADING -> appendHeading(body, block);
                    case PARAGRAPH -> appendParagraph(body, nullToEmpty(block.getText()));
                    case BULLET_ITEM -> {
                        orderedIndex = 0;
                        appendParagraph(body, "• " + nullToEmpty(block.getText()));
                    }
                    case ORDERED_ITEM -> {
                        orderedIndex++;
                        appendParagraph(body, orderedIndex + ". " + nullToEmpty(block.getText()));
                    }
                    case TABLE -> appendTable(body, block);
                    case IMAGE -> embeddedBytes = appendImage(body, ctx.getWorkspaceId(), block, resolver, embeddedBytes);
                }
            }

            String html = wrapHtmlDocument(body.toString());
            byte[] pdfBytes = renderPdf(html);
            return ExportArtifact.builder()
                .bytes(pdfBytes)
                .contentType(ExportFormat.PDF.getContentType())
                .fileName(ExportFileNames.build(ctx.getTitle(), "pdf"))
                .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.EXPORT_RENDER_FAILED);
        }
    }

    private byte[] renderPdf(String html) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        registerChineseFontIfPresent(builder);
        builder.toStream(out);
        builder.run();
        return out.toByteArray();
    }

    private static void registerChineseFontIfPresent(PdfRendererBuilder builder) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = PdfNodeExporter.class.getClassLoader();
        }
        if (cl.getResource(FONT_RESOURCE) == null) {
            return;
        }
        ClassLoader fontLoader = cl;
        builder.useFont(() -> {
            InputStream in = fontLoader.getResourceAsStream(FONT_RESOURCE);
            if (in == null) {
                throw new IllegalStateException("Missing font resource: " + FONT_RESOURCE);
            }
            return in;
        }, FONT_FAMILY);
    }

    private static String wrapHtmlDocument(String bodyHtml) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8"/>
            <style>
            body { font-family: '%s', sans-serif; }
            img { max-width: 100%%; }
            table { border-collapse: collapse; width: 100%%; }
            td, th { border: 1px solid #333; padding: 4px; }
            </style>
            </head>
            <body>
            %s
            </body>
            </html>
            """.formatted(FONT_FAMILY, bodyHtml);
    }

    private static void appendHeading(StringBuilder body, MarkdownBlock block) {
        int level = block.getLevel() == null ? 1 : Math.min(6, Math.max(1, block.getLevel()));
        body.append("<h").append(level).append('>')
            .append(escape(nullToEmpty(block.getText())))
            .append("</h").append(level).append('>');
    }

    private static void appendParagraph(StringBuilder body, String text) {
        body.append("<p>").append(escape(text)).append("</p>");
    }

    private static void appendTable(StringBuilder body, MarkdownBlock block) {
        List<List<String>> rows = block.getTableRows();
        if (rows == null || rows.isEmpty()) {
            return;
        }
        body.append("<table>");
        for (List<String> cells : rows) {
            body.append("<tr>");
            if (cells != null) {
                for (String cell : cells) {
                    body.append("<td>").append(escape(nullToEmpty(cell))).append("</td>");
                }
            }
            body.append("</tr>");
        }
        body.append("</table>");
    }

    private long appendImage(
        StringBuilder body,
        Long workspaceId,
        MarkdownBlock block,
        ExportAssetResolver resolver,
        long embeddedBytes
    ) {
        ResolvedExportAsset asset = resolver == null ? null : resolver.resolve(workspaceId, block.getUrl());
        String contentType = asset == null ? null : supportedImageContentType(asset.getContentType());
        if (asset == null || asset.getBytes() == null || asset.getBytes().length == 0 || contentType == null) {
            body.append("<p><em>").append(escape(MISSING_IMAGE_PLACEHOLDER)).append("</em></p>");
            return embeddedBytes;
        }

        long next = embeddedBytes + asset.getBytes().length;
        if (next > properties.getMaxEmbeddedImageBytes()) {
            throw new BusinessException(BizResponseCode.EXPORT_IMAGES_TOO_LARGE);
        }

        String base64 = Base64.getEncoder().encodeToString(asset.getBytes());
        String alt = escape(nullToEmpty(block.getAlt()));
        body.append("<p><img src=\"data:")
            .append(contentType)
            .append(";base64,")
            .append(base64)
            .append("\" alt=\"")
            .append(alt)
            .append("\"/></p>");
        return next;
    }

    /**
     * Returns a normalized MIME type for JPEG/PNG/GIF, or {@code null} when unsupported (e.g. WebP).
     */
    private static String supportedImageContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String ct = contentType.toLowerCase();
        if (ct.contains("jpeg") || ct.contains("jpg")) {
            return "image/jpeg";
        }
        if (ct.contains("png")) {
            return "image/png";
        }
        if (ct.contains("gif")) {
            return "image/gif";
        }
        return null;
    }

    private static String escape(String value) {
        return HtmlUtils.htmlEscape(value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

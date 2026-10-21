package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.biz.export.markdown.MarkdownHtmlConverter;
import cn.guet.soft_manage.biz.export.util.ExportFileNames;
import cn.guet.soft_manage.biz.export.util.ImageMediaTypes;
import cn.guet.soft_manage.frame.config.ExportProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports DOCUMENT nodes to DOCX via TipTap markdown → HTML → POI.
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
        String html = MarkdownHtmlConverter.toHtml(contentMd);

        try (XWPFDocument document = new XWPFDocument()) {
            long embeddedBytes = 0;
            org.jsoup.nodes.Document dom = Jsoup.parseBodyFragment(html);
            for (Element child : dom.body().children()) {
                embeddedBytes = writeBlock(document, child, ctx.getWorkspaceId(), resolver, embeddedBytes);
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

    private long writeBlock(
        XWPFDocument document,
        Element el,
        Long workspaceId,
        ExportAssetResolver resolver,
        long embeddedBytes
    ) throws Exception {
        String tag = el.normalName();
        return switch (tag) {
            case "h1", "h2", "h3", "h4", "h5", "h6" -> {
                int level = tag.charAt(1) - '0';
                XWPFParagraph p = document.createParagraph();
                appendInline(p, el.childNodes(), false, false, level);
                yield embeddedBytes;
            }
            case "p" -> writeParagraphElement(document, el, workspaceId, resolver, embeddedBytes);
            case "ul", "ol" -> writeList(document, el, "ol".equals(tag), workspaceId, resolver, embeddedBytes);
            case "table" -> {
                writeTable(document, el);
                yield embeddedBytes;
            }
            case "img" -> appendImage(document, workspaceId, el, resolver, embeddedBytes);
            case "blockquote", "div", "section", "article" -> {
                long bytes = embeddedBytes;
                if (el.children().isEmpty()) {
                    if (!el.ownText().isBlank()) {
                        XWPFParagraph p = document.createParagraph();
                        appendInline(p, el.childNodes(), false, false, 0);
                    }
                } else {
                    for (Element child : el.children()) {
                        bytes = writeBlock(document, child, workspaceId, resolver, bytes);
                    }
                }
                yield bytes;
            }
            case "pre" -> {
                XWPFParagraph p = document.createParagraph();
                XWPFRun run = p.createRun();
                run.setFontFamily("Courier New");
                run.setText(el.text());
                yield embeddedBytes;
            }
            case "hr" -> {
                document.createParagraph();
                yield embeddedBytes;
            }
            default -> writeParagraphElement(document, el, workspaceId, resolver, embeddedBytes);
        };
    }

    private long writeParagraphElement(
        XWPFDocument document,
        Element el,
        Long workspaceId,
        ExportAssetResolver resolver,
        long embeddedBytes
    ) throws Exception {
        List<Node> buffer = new ArrayList<>();
        for (Node node : el.childNodes()) {
            if (node instanceof Element child && "img".equals(child.normalName())) {
                if (hasVisibleContent(buffer)) {
                    XWPFParagraph p = document.createParagraph();
                    appendInline(p, buffer, false, false, 0);
                }
                buffer = new ArrayList<>();
                embeddedBytes = appendImage(document, workspaceId, child, resolver, embeddedBytes);
            } else {
                buffer.add(node);
            }
        }
        if (hasVisibleContent(buffer) || el.childNodes().isEmpty()) {
            XWPFParagraph p = document.createParagraph();
            appendInline(p, buffer, false, false, 0);
        }
        return embeddedBytes;
    }

    private static boolean hasVisibleContent(List<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof TextNode tn && !tn.text().isBlank()) {
                return true;
            }
            if (node instanceof Element el && !el.text().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private long writeList(
        XWPFDocument document,
        Element list,
        boolean ordered,
        Long workspaceId,
        ExportAssetResolver resolver,
        long embeddedBytes
    ) throws Exception {
        int index = 0;
        for (Element li : list.children()) {
            if (!"li".equals(li.normalName())) {
                continue;
            }
            index++;
            XWPFParagraph p = document.createParagraph();
            XWPFRun prefix = p.createRun();
            prefix.setText(ordered ? (index + ". ") : "• ");

            List<Node> buffer = new ArrayList<>();
            for (Node node : li.childNodes()) {
                if (node instanceof Element child && "img".equals(child.normalName())) {
                    if (hasVisibleContent(buffer)) {
                        appendInline(p, buffer, false, false, 0);
                        buffer = new ArrayList<>();
                        p = document.createParagraph();
                    }
                    embeddedBytes = appendImage(document, workspaceId, child, resolver, embeddedBytes);
                } else if (node instanceof Element child && ("ul".equals(child.normalName()) || "ol".equals(child.normalName()))) {
                    if (hasVisibleContent(buffer)) {
                        appendInline(p, buffer, false, false, 0);
                        buffer = new ArrayList<>();
                    }
                    embeddedBytes = writeList(document, child, "ol".equals(child.normalName()), workspaceId, resolver, embeddedBytes);
                } else {
                    buffer.add(node);
                }
            }
            if (hasVisibleContent(buffer)) {
                appendInline(p, buffer, false, false, 0);
            }
        }
        return embeddedBytes;
    }

    private static void writeTable(XWPFDocument document, Element tableEl) {
        Elements rows = tableEl.select("> thead > tr, > tbody > tr, > tr");
        if (rows.isEmpty()) {
            return;
        }
        int cols = 0;
        for (Element row : rows) {
            cols = Math.max(cols, row.select("> th, > td").size());
        }
        if (cols == 0) {
            return;
        }
        XWPFTable table = document.createTable(rows.size(), cols);
        for (int r = 0; r < rows.size(); r++) {
            XWPFTableRow row = table.getRow(r);
            Elements cells = rows.get(r).select("> th, > td");
            for (int c = 0; c < cols; c++) {
                XWPFTableCell cell = row.getCell(c);
                String value = c < cells.size() ? cells.get(c).text() : "";
                cell.removeParagraph(0);
                XWPFParagraph p = cell.addParagraph();
                XWPFRun run = p.createRun();
                run.setText(value);
                if (c < cells.size() && "th".equals(cells.get(c).normalName())) {
                    run.setBold(true);
                }
            }
        }
    }

    private static void appendInline(
        XWPFParagraph paragraph,
        List<Node> nodes,
        boolean bold,
        boolean italic,
        int headingLevel
    ) {
        for (Node node : nodes) {
            if (node instanceof TextNode textNode) {
                String text = textNode.text();
                if (text.isEmpty()) {
                    continue;
                }
                XWPFRun run = paragraph.createRun();
                applyStyle(run, bold, italic, headingLevel);
                run.setText(text);
            } else if (node instanceof Element el) {
                String tag = el.normalName();
                switch (tag) {
                    case "strong", "b" -> appendInline(paragraph, el.childNodes(), true, italic, headingLevel);
                    case "em", "i" -> appendInline(paragraph, el.childNodes(), bold, true, headingLevel);
                    case "br" -> paragraph.createRun().addBreak();
                    case "code" -> {
                        XWPFRun run = paragraph.createRun();
                        run.setFontFamily("Courier New");
                        run.setText(el.text());
                    }
                    case "img" -> {
                        // images are handled by paragraph splitter
                    }
                    default -> appendInline(paragraph, el.childNodes(), bold, italic, headingLevel);
                }
            }
        }
    }

    private static void applyStyle(XWPFRun run, boolean bold, boolean italic, int headingLevel) {
        if (headingLevel > 0) {
            run.setBold(true);
            run.setFontSize(Math.max(12, 24 - (headingLevel - 1) * 2));
        } else if (bold) {
            run.setBold(true);
        }
        if (italic) {
            run.setItalic(true);
        }
    }

    private long appendImage(
        XWPFDocument document,
        Long workspaceId,
        Element img,
        ExportAssetResolver resolver,
        long embeddedBytes
    ) throws Exception {
        String src = img.hasAttr("src") ? img.attr("src") : null;
        ResolvedExportAsset asset = resolver == null ? null : resolver.resolve(workspaceId, src);
        String mediaType = asset == null ? null : ImageMediaTypes.sniff(asset.getBytes(), asset.getContentType());
        Integer pictureType = toPoiPictureType(mediaType);
        if (asset == null || asset.getBytes() == null || asset.getBytes().length == 0 || pictureType == null) {
            appendMissingImagePlaceholder(document);
            return embeddedBytes;
        }

        long next = embeddedBytes + asset.getBytes().length;
        if (next > properties.getMaxEmbeddedImageBytes()) {
            throw new BusinessException(BizResponseCode.EXPORT_IMAGES_TOO_LARGE);
        }

        int widthPx = parsePx(img.attr("width"), DEFAULT_IMAGE_WIDTH_PX);
        int heightPx = parsePx(img.attr("height"), DEFAULT_IMAGE_HEIGHT_PX);

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        String fileName = asset.getFileName() != null && !asset.getFileName().isBlank()
            ? asset.getFileName()
            : "image";
        try (ByteArrayInputStream in = new ByteArrayInputStream(asset.getBytes())) {
            run.addPicture(
                in,
                pictureType,
                fileName,
                Units.pixelToEMU(widthPx),
                Units.pixelToEMU(heightPx)
            );
        }
        return next;
    }

    private static void appendMissingImagePlaceholder(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setItalic(true);
        run.setText(MISSING_IMAGE_PLACEHOLDER);
    }

    private static Integer toPoiPictureType(String mediaType) {
        if (mediaType == null) {
            return null;
        }
        return switch (mediaType) {
            case "image/jpeg" -> XWPFDocument.PICTURE_TYPE_JPEG;
            case "image/png" -> XWPFDocument.PICTURE_TYPE_PNG;
            case "image/gif" -> XWPFDocument.PICTURE_TYPE_GIF;
            case "image/bmp" -> XWPFDocument.PICTURE_TYPE_BMP;
            default -> null;
        };
    }

    private static int parsePx(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return fallback;
        }
        try {
            int v = Integer.parseInt(digits);
            return v > 0 ? Math.min(v, 2000) : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}

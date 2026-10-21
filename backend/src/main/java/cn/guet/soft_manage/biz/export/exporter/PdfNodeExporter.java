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
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * Exports DOCUMENT nodes to PDF: TipTap markdown → HTML → openhtmltopdf.
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
        String htmlBody = MarkdownHtmlConverter.toHtml(contentMd);
        String rewritten = rewriteImages(htmlBody, ctx.getWorkspaceId(), resolver);
        String html = wrapHtmlDocument(rewritten);

        try {
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

    private String rewriteImages(String html, Long workspaceId, ExportAssetResolver resolver) {
        org.jsoup.nodes.Document dom = Jsoup.parseBodyFragment(html);
        // openhtmltopdf requires XHTML (self-closing <img />); HTML5 <img></img> breaks the XML parser
        dom.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        Elements images = dom.select("img");
        long embeddedBytes = 0;
        for (Element img : images) {
            String src = img.hasAttr("src") ? img.attr("src") : null;
            ResolvedExportAsset asset = resolver == null ? null : resolver.resolve(workspaceId, src);
            String mediaType = asset == null ? null : ImageMediaTypes.sniff(asset.getBytes(), asset.getContentType());
            if (asset == null || asset.getBytes() == null || asset.getBytes().length == 0 || mediaType == null) {
                img.replaceWith(new org.jsoup.nodes.Element("em").text(MISSING_IMAGE_PLACEHOLDER));
                continue;
            }
            long next = embeddedBytes + asset.getBytes().length;
            if (next > properties.getMaxEmbeddedImageBytes()) {
                throw new BusinessException(BizResponseCode.EXPORT_IMAGES_TOO_LARGE);
            }
            embeddedBytes = next;
            String base64 = Base64.getEncoder().encodeToString(asset.getBytes());
            img.attr("src", "data:" + mediaType + ";base64," + base64);
        }
        return dom.body().html();
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
}

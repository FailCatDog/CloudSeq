package cn.guet.soft_manage.biz.export.markdown;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.List;

/**
 * TipTap {@code getMarkdown()} output → HTML.
 * <p>
 * Keeps raw HTML fragments (e.g. resized {@code <img width height>}) and turns
 * {@code **bold**} into {@code <strong>}.
 */
public final class MarkdownHtmlConverter {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        options.set(HtmlRenderer.ESCAPE_HTML, false);
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    private MarkdownHtmlConverter() {
    }

    public static String toHtml(String markdown) {
        return RENDERER.render(PARSER.parse(markdown == null ? "" : markdown));
    }
}

package cn.guet.soft_manage.biz.export.markdown;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Markdown into a flat list of {@link MarkdownBlock} for DOCX/PDF export.
 * <p>
 * TipTap {@code ProjectDocImage} may serialize resized images as raw HTML
 * {@code <img src="..." width="..." />} instead of {@code ![alt](url)}; both forms
 * are recognized here.
 */
public final class MarkdownDocumentParser {

    private static final Parser PARSER = createParser();

    /** Matches TipTap / HTML img tags; captures src then optional alt. */
    private static final Pattern IMG_TAG = Pattern.compile(
        "<img\\b([^>]*)>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern ATTR = Pattern.compile(
        "\\b(src|alt)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))",
        Pattern.CASE_INSENSITIVE
    );

    private static Parser createParser() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        return Parser.builder(options).build();
    }

    private MarkdownDocumentParser() {
    }

    public static List<MarkdownBlock> parse(String markdown) {
        Node document = PARSER.parse(markdown == null ? "" : markdown);
        List<MarkdownBlock> blocks = new ArrayList<>();
        for (Node child : document.getChildren()) {
            appendTopLevelBlock(child, blocks);
        }
        return blocks;
    }

    private static void appendTopLevelBlock(Node node, List<MarkdownBlock> blocks) {
        if (node instanceof Heading heading) {
            blocks.add(MarkdownBlock.builder()
                .type(MarkdownBlock.Type.HEADING)
                .level(heading.getLevel())
                .text(collectText(heading))
                .build());
            return;
        }
        if (node instanceof Paragraph paragraph) {
            appendParagraphBlock(paragraph, blocks);
            return;
        }
        if (node instanceof HtmlBlock htmlBlock) {
            appendImagesFromHtml(htmlBlock.getChars().toString(), blocks);
            return;
        }
        if (node instanceof BulletList bulletList) {
            appendListItems(bulletList, MarkdownBlock.Type.BULLET_ITEM, blocks);
            return;
        }
        if (node instanceof OrderedList orderedList) {
            appendListItems(orderedList, MarkdownBlock.Type.ORDERED_ITEM, blocks);
            return;
        }
        if (node instanceof TableBlock tableBlock) {
            blocks.add(parseTable(tableBlock));
        }
    }

    private static void appendParagraphBlock(Paragraph paragraph, List<MarkdownBlock> blocks) {
        Image loneImage = loneMarkdownImage(paragraph);
        if (loneImage != null) {
            blocks.add(imageBlock(loneImage));
            return;
        }

        List<MarkdownBlock> htmlImages = new ArrayList<>();
        boolean onlyHtmlImages = true;
        for (Node child : paragraph.getChildren()) {
            if (child instanceof HtmlInline htmlInline) {
                int before = htmlImages.size();
                appendImagesFromHtml(htmlInline.getChars().toString(), htmlImages);
                if (htmlImages.size() == before && hasVisibleText(child)) {
                    onlyHtmlImages = false;
                }
            } else if (child instanceof Image image) {
                htmlImages.add(imageBlock(image));
            } else if (hasVisibleText(child)) {
                onlyHtmlImages = false;
            }
        }

        if (onlyHtmlImages && !htmlImages.isEmpty()) {
            blocks.addAll(htmlImages);
            return;
        }

        if (!htmlImages.isEmpty()) {
            // Mixed content: keep text paragraph, then emit images so they are not dropped.
            String text = collectText(paragraph);
            if (text != null && !text.isBlank()) {
                blocks.add(MarkdownBlock.builder()
                    .type(MarkdownBlock.Type.PARAGRAPH)
                    .text(text)
                    .build());
            }
            blocks.addAll(htmlImages);
            return;
        }

        blocks.add(MarkdownBlock.builder()
            .type(MarkdownBlock.Type.PARAGRAPH)
            .text(collectText(paragraph))
            .build());
    }

    private static Image loneMarkdownImage(Paragraph paragraph) {
        Image found = null;
        for (Node child : paragraph.getChildren()) {
            if (child instanceof Image image) {
                if (found != null) {
                    return null;
                }
                found = image;
            } else if (child instanceof HtmlInline) {
                return null;
            } else if (hasVisibleText(child)) {
                return null;
            }
        }
        return found;
    }

    private static boolean hasVisibleText(Node node) {
        String text = collectText(node);
        return text != null && !text.isBlank();
    }

    private static void appendListItems(Node listNode, MarkdownBlock.Type itemType, List<MarkdownBlock> blocks) {
        for (Node child : listNode.getChildren()) {
            if (child instanceof ListItem listItem) {
                blocks.add(MarkdownBlock.builder()
                    .type(itemType)
                    .text(collectText(listItem))
                    .build());
            }
        }
    }

    private static MarkdownBlock parseTable(TableBlock tableBlock) {
        List<List<String>> rows = new ArrayList<>();
        for (Node section : tableBlock.getChildren()) {
            if (section instanceof TableHead || section instanceof TableBody) {
                for (Node rowNode : section.getChildren()) {
                    if (rowNode instanceof TableRow tableRow) {
                        rows.add(parseTableRow(tableRow));
                    }
                }
            }
        }
        return MarkdownBlock.builder()
            .type(MarkdownBlock.Type.TABLE)
            .tableRows(rows)
            .build();
    }

    private static List<String> parseTableRow(TableRow tableRow) {
        List<String> cells = new ArrayList<>();
        for (Node cellNode : tableRow.getChildren()) {
            if (cellNode instanceof TableCell tableCell) {
                cells.add(collectText(tableCell));
            }
        }
        return cells;
    }

    private static MarkdownBlock imageBlock(Image image) {
        return MarkdownBlock.builder()
            .type(MarkdownBlock.Type.IMAGE)
            .url(image.getUrl().toString())
            .alt(image.getText().toString())
            .build();
    }

    static void appendImagesFromHtml(String html, List<MarkdownBlock> blocks) {
        if (html == null || html.isBlank()) {
            return;
        }
        Matcher matcher = IMG_TAG.matcher(html);
        while (matcher.find()) {
            String attrs = matcher.group(1);
            String src = null;
            String alt = "";
            Matcher attrMatcher = ATTR.matcher(attrs);
            while (attrMatcher.find()) {
                String name = attrMatcher.group(1).toLowerCase();
                String value = firstNonNull(attrMatcher.group(2), attrMatcher.group(3), attrMatcher.group(4));
                if ("src".equals(name)) {
                    src = value;
                } else if ("alt".equals(name) && value != null) {
                    alt = value;
                }
            }
            if (src != null && !src.isBlank()) {
                blocks.add(MarkdownBlock.builder()
                    .type(MarkdownBlock.Type.IMAGE)
                    .url(src.trim())
                    .alt(alt)
                    .build());
            }
        }
    }

    private static String firstNonNull(String a, String b, String c) {
        if (a != null) {
            return a;
        }
        if (b != null) {
            return b;
        }
        return c;
    }

    private static String collectText(Node node) {
        // TextCollectingVisitor is mutable; allocate per call for thread safety.
        return new TextCollectingVisitor().collectAndGetText(node).trim();
    }
}

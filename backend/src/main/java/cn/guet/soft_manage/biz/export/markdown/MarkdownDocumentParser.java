package cn.guet.soft_manage.biz.export.markdown;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Heading;
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

/**
 * Parses Markdown into a flat list of {@link MarkdownBlock} for DOCX/PDF export.
 */
public final class MarkdownDocumentParser {

    private static final Parser PARSER = createParser();

    private static Parser createParser() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        return Parser.builder(options).build();
    }
    private static final TextCollectingVisitor TEXT = new TextCollectingVisitor();

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
        Image loneImage = loneImage(paragraph);
        if (loneImage != null) {
            blocks.add(imageBlock(loneImage));
            return;
        }
        blocks.add(MarkdownBlock.builder()
            .type(MarkdownBlock.Type.PARAGRAPH)
            .text(collectText(paragraph))
            .build());
    }

    private static Image loneImage(Paragraph paragraph) {
        Image found = null;
        for (Node child : paragraph.getChildren()) {
            if (child instanceof Image image) {
                if (found != null) {
                    return null;
                }
                found = image;
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

    private static String collectText(Node node) {
        return TEXT.collectAndGetText(node).trim();
    }
}

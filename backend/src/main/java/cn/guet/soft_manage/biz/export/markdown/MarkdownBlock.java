package cn.guet.soft_manage.biz.export.markdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One logical block parsed from Markdown for export rendering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkdownBlock {

    public enum Type {
        HEADING,
        PARAGRAPH,
        BULLET_ITEM,
        ORDERED_ITEM,
        TABLE,
        IMAGE
    }

    private Type type;

    /** Heading level (1–6); only for {@link Type#HEADING}. */
    private Integer level;

    /** Plain text content for headings, paragraphs, and list items. */
    private String text;

    /** Image URL; only for {@link Type#IMAGE}. */
    private String url;

    /** Image alt text; only for {@link Type#IMAGE}. */
    private String alt;

    /** Table rows (each row is a list of cell texts); only for {@link Type#TABLE}. */
    private List<List<String>> tableRows;
}

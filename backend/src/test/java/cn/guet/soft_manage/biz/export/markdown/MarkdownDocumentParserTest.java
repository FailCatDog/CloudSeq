package cn.guet.soft_manage.biz.export.markdown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownDocumentParserTest {

    @Test
    void parsesHeadingParagraphAndImage() {
        var blocks = MarkdownDocumentParser.parse(
            "# Title\n\nHello\n\n![alt](/api/assets/12)\n");
        assertEquals(MarkdownBlock.Type.HEADING, blocks.get(0).getType());
        assertEquals(1, blocks.get(0).getLevel());
        assertEquals(MarkdownBlock.Type.PARAGRAPH, blocks.get(1).getType());
        assertEquals(MarkdownBlock.Type.IMAGE, blocks.get(2).getType());
        assertEquals("/api/assets/12", blocks.get(2).getUrl());
    }
}

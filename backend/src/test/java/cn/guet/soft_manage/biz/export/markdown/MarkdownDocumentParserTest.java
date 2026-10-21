package cn.guet.soft_manage.biz.export.markdown;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownDocumentParserTest {

    @Test
    void parsesHeadingParagraphAndImage() {
        var blocks = MarkdownDocumentParser.parse(
            "# Title\n\nHello\n\n![alt](/api/assets/12)\n");
        assertEquals(MarkdownBlock.Type.HEADING, blocks.get(0).getType());
        assertEquals(1, blocks.get(0).getLevel());
        assertEquals(MarkdownBlock.Type.PARAGRAPH, blocks.get(1).getType());
        assertEquals("Hello", blocks.get(1).getText());
        assertEquals(MarkdownBlock.Type.IMAGE, blocks.get(2).getType());
        assertEquals("/api/assets/12", blocks.get(2).getUrl());
    }

    @Test
    void parsesTipTapHtmlImgWithDimensions() {
        // TipTap ProjectDocImage.renderMarkdown emits HTML when width/height are set
        var blocks = MarkdownDocumentParser.parse(
            "# Title\n\n"
                + "<img class=\"ps-doc-image\" src=\"/api/assets/99\" alt=\"shot.png\" width=\"400\" height=\"300\" />\n");
        assertEquals(MarkdownBlock.Type.HEADING, blocks.get(0).getType());
        assertEquals(MarkdownBlock.Type.IMAGE, blocks.get(1).getType());
        assertEquals("/api/assets/99", blocks.get(1).getUrl());
        assertEquals("shot.png", blocks.get(1).getAlt());
    }

    @Test
    void parsesHtmlImgSrcWithQueryString() {
        var blocks = MarkdownDocumentParser.parse(
            "<img src=\"/api/assets/42?access_token=abc\" alt=\"x\" />\n");
        assertEquals(MarkdownBlock.Type.IMAGE, blocks.get(0).getType());
        assertEquals("/api/assets/42?access_token=abc", blocks.get(0).getUrl());
    }

    @Test
    void collectTextIsThreadSafeUnderConcurrentParse() throws Exception {
        int threads = 8;
        var executor = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                int n = i;
                tasks.add(() -> {
                    var blocks = MarkdownDocumentParser.parse("# H" + n + "\n\nBody" + n + "\n");
                    assertEquals(MarkdownBlock.Type.HEADING, blocks.get(0).getType());
                    assertEquals("H" + n, blocks.get(0).getText());
                    assertEquals("Body" + n, blocks.get(1).getText());
                    return blocks.get(0).getText();
                });
            }
            List<Future<String>> futures = executor.invokeAll(tasks);
            for (int i = 0; i < threads; i++) {
                assertEquals("H" + i, futures.get(i).get(5, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}

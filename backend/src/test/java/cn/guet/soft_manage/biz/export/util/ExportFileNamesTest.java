package cn.guet.soft_manage.biz.export.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportFileNamesTest {

    @Test
    void stripsPathSeparatorsAndKeepsExtension() {
        assertEquals("报告.docx", ExportFileNames.build("a/../报告", "docx"));
    }

    @Test
    void blankTitleFallsBackToNode() {
        assertEquals("node.pdf", ExportFileNames.build("  ", "pdf"));
    }
}

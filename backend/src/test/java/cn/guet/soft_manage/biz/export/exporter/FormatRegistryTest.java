package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormatRegistryTest {
    @Mock NodeExporter docx;
    @Mock NodeExporter pdf;
    @Mock NodeExporter xlsx;

    @Test
    void resolvesDocumentDocx() {
        when(docx.supports("DOCUMENT", ExportFormat.DOCX)).thenReturn(true);
        FormatRegistry registry = new FormatRegistry(List.of(docx, pdf, xlsx));
        assertSame(docx, registry.resolve("DOCUMENT", ExportFormat.DOCX));
    }

    @Test
    void rejectsSheetDocx() {
        when(docx.supports("SHEET", ExportFormat.DOCX)).thenReturn(false);
        when(pdf.supports("SHEET", ExportFormat.DOCX)).thenReturn(false);
        when(xlsx.supports("SHEET", ExportFormat.DOCX)).thenReturn(false);
        FormatRegistry registry = new FormatRegistry(List.of(docx, pdf, xlsx));
        BusinessException ex = assertThrows(BusinessException.class,
            () -> registry.resolve("SHEET", ExportFormat.DOCX));
        assertEquals(BizResponseCode.EXPORT_FORMAT_INVALID.getCode(), ex.getCode());
    }
}

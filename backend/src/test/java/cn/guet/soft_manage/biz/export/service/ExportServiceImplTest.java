package cn.guet.soft_manage.biz.export.service;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import cn.guet.soft_manage.biz.export.delivery.ExportDelivery;
import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.exporter.FormatRegistry;
import cn.guet.soft_manage.biz.export.exporter.NodeExporter;
import cn.guet.soft_manage.biz.export.service.impl.ExportServiceImpl;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceAccessService;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock WorkspaceNodeDao workspaceNodeDao;
    @Mock WorkspaceContentDao workspaceContentDao;
    @Mock WorkspaceAccessService workspaceAccessService;
    @Mock FormatRegistry formatRegistry;
    @Mock ExportDelivery exportDelivery;
    @Mock ExportAssetResolver exportAssetResolver;
    @Mock NodeExporter nodeExporter;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportServiceImpl(
            workspaceNodeDao,
            workspaceContentDao,
            workspaceAccessService,
            formatRegistry,
            exportDelivery,
            exportAssetResolver
        );
    }

    @Test
    void exportsDocumentDocxHappyPath() {
        Long nodeId = 10L;
        Long workspaceId = 20L;
        WorkspaceNode node = WorkspaceNode.builder()
            .id(nodeId)
            .workspaceId(workspaceId)
            .nodeType("DOCUMENT")
            .title("Spec")
            .build();
        WorkspaceContent content = WorkspaceContent.builder()
            .nodeId(nodeId)
            .contentMd("# Hello")
            .build();
        ExportArtifact rendered = ExportArtifact.builder()
            .bytes(new byte[]{1, 2, 3})
            .contentType(ExportFormat.DOCX.getContentType())
            .fileName("Spec.docx")
            .build();
        ExportArtifact delivered = ExportArtifact.builder()
            .bytes(new byte[]{1, 2, 3})
            .contentType(ExportFormat.DOCX.getContentType())
            .fileName("Spec.docx")
            .build();

        when(workspaceNodeDao.selectById(nodeId)).thenReturn(node);
        when(workspaceAccessService.requireCurrentAccess(workspaceId))
            .thenReturn(WorkspaceAccessContext.builder().canWrite(false).build());
        when(workspaceContentDao.selectOne(any(Wrapper.class))).thenReturn(content);
        when(formatRegistry.resolve("DOCUMENT", ExportFormat.DOCX)).thenReturn(nodeExporter);
        when(nodeExporter.export(any(ExportContext.class))).thenReturn(rendered);
        when(exportDelivery.deliver(rendered)).thenReturn(delivered);

        ExportArtifact result = exportService.export(nodeId, "docx");

        assertSame(delivered, result);
        verify(workspaceAccessService).requireCurrentAccess(workspaceId);

        ArgumentCaptor<ExportContext> ctxCaptor = ArgumentCaptor.forClass(ExportContext.class);
        verify(nodeExporter).export(ctxCaptor.capture());
        ExportContext ctx = ctxCaptor.getValue();
        assertEquals(nodeId, ctx.getNodeId());
        assertEquals(workspaceId, ctx.getWorkspaceId());
        assertEquals("DOCUMENT", ctx.getNodeType());
        assertEquals("Spec", ctx.getTitle());
        assertEquals("# Hello", ctx.getContentMd());
        assertSame(exportAssetResolver, ctx.getAssetResolver());

        verify(exportDelivery).deliver(rendered);
        assertArrayEquals(new byte[]{1, 2, 3}, result.getBytes());
        assertEquals("Spec.docx", result.getFileName());
    }

    @Test
    void rejectsUnknownFormat() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> exportService.export(10L, "unknown"));
        assertEquals(BizResponseCode.EXPORT_FORMAT_INVALID.getCode(), ex.getCode());
        verifyNoInteractions(workspaceNodeDao, workspaceAccessService, workspaceContentDao,
            formatRegistry, exportDelivery, nodeExporter);
    }

    @Test
    void rejectsNullFormat() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> exportService.export(10L, null));
        assertEquals(BizResponseCode.EXPORT_FORMAT_INVALID.getCode(), ex.getCode());
        verifyNoInteractions(workspaceNodeDao);
    }

    @Test
    void rejectsMissingNode() {
        when(workspaceNodeDao.selectById(10L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> exportService.export(10L, "docx"));
        assertEquals(BizResponseCode.NODE_NOT_FOUND.getCode(), ex.getCode());
        verifyNoInteractions(workspaceAccessService, workspaceContentDao, formatRegistry);
    }

    @Test
    void rejectsMissingContent() {
        Long nodeId = 10L;
        Long workspaceId = 20L;
        WorkspaceNode node = WorkspaceNode.builder()
            .id(nodeId)
            .workspaceId(workspaceId)
            .nodeType("DOCUMENT")
            .title("Spec")
            .build();
        when(workspaceNodeDao.selectById(nodeId)).thenReturn(node);
        when(workspaceAccessService.requireCurrentAccess(workspaceId))
            .thenReturn(WorkspaceAccessContext.builder().canWrite(false).build());
        when(workspaceContentDao.selectOne(any(Wrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> exportService.export(nodeId, "docx"));
        assertEquals(BizResponseCode.DOCUMENT_CONTENT_NOT_FOUND.getCode(), ex.getCode());
        verify(workspaceAccessService).requireCurrentAccess(eq(workspaceId));
        verifyNoInteractions(formatRegistry, exportDelivery);
    }

    @Test
    void callsRequireCurrentAccessBeforeContentLoad() {
        Long nodeId = 10L;
        Long workspaceId = 20L;
        WorkspaceNode node = WorkspaceNode.builder()
            .id(nodeId)
            .workspaceId(workspaceId)
            .nodeType("DOCUMENT")
            .title("Spec")
            .build();
        when(workspaceNodeDao.selectById(nodeId)).thenReturn(node);
        when(workspaceAccessService.requireCurrentAccess(workspaceId))
            .thenThrow(new BusinessException(BizResponseCode.FORBIDDEN));

        assertThrows(BusinessException.class, () -> exportService.export(nodeId, "docx"));
        verify(workspaceAccessService).requireCurrentAccess(workspaceId);
        verifyNoInteractions(workspaceContentDao, formatRegistry, exportDelivery);
    }
}

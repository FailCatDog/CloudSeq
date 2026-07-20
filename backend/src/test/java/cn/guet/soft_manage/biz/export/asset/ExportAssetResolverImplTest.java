package cn.guet.soft_manage.biz.export.asset;

import cn.guet.soft_manage.biz.document.dao.WorkspaceAssetDao;
import cn.guet.soft_manage.biz.document.entity.WorkspaceAsset;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.frame.constant.MinioConstants;
import cn.guet.soft_manage.frame.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportAssetResolverImplTest {
    @Mock WorkspaceAssetDao assetDao;
    @Mock ObjectStorageService storage;
    ExportAssetResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExportAssetResolverImpl(assetDao, storage);
    }

    @Test
    void parsesRelativeAssetUrl() throws Exception {
        WorkspaceAsset asset = WorkspaceAsset.builder()
            .id(9L).workspaceId(1L).storageKey("k").contentType("image/png")
            .originalName("a.png").sizeBytes(3L).build();
        when(assetDao.selectById(9L)).thenReturn(asset);
        when(storage.getObject(eq(MinioConstants.BUCKET_WORKSPACE_ASSETS), eq("k")))
            .thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        ResolvedExportAsset got = resolver.resolve(1L, "/api/assets/9");
        assertNotNull(got);
        assertEquals(3, got.getBytes().length);
    }

    @Test
    void rejectsExternalUrl() {
        assertNull(resolver.resolve(1L, "https://evil.example/x.png"));
    }

    @Test
    void rejectsOtherWorkspace() {
        WorkspaceAsset asset = WorkspaceAsset.builder()
            .id(9L).workspaceId(2L).storageKey("k").build();
        when(assetDao.selectById(9L)).thenReturn(asset);
        assertNull(resolver.resolve(1L, "/api/assets/9"));
    }
}

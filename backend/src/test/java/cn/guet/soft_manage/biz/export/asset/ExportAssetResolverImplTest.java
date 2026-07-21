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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportAssetResolverImplTest {
    @Mock WorkspaceAssetDao assetDao;
    @Mock ObjectStorageService storage;
    @Mock SafeExternalImageFetcher externalImageFetcher;
    ExportAssetResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExportAssetResolverImpl(assetDao, storage, externalImageFetcher);
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
        verify(externalImageFetcher, never()).fetch(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resolvesExternalHttpUrlViaFetcher() {
        ResolvedExportAsset fetched = ResolvedExportAsset.builder()
            .bytes(new byte[]{1, 2, 3})
            .contentType("image/png")
            .fileName("a.png")
            .build();
        when(externalImageFetcher.fetch("https://pdai.tech/images/arch_sqlyuanli.png")).thenReturn(fetched);

        ResolvedExportAsset got = resolver.resolve(1L, "https://pdai.tech/images/arch_sqlyuanli.png");
        assertNotNull(got);
        assertEquals("image/png", got.getContentType());
        verify(externalImageFetcher).fetch("https://pdai.tech/images/arch_sqlyuanli.png");
    }

    @Test
    void returnsNullWhenExternalFetchFails() {
        when(externalImageFetcher.fetch("https://evil.example/x.png")).thenReturn(null);
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

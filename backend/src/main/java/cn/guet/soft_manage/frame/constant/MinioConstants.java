package cn.guet.soft_manage.frame.constant;

/**
 * MinIO 桶名与对象路径常量
 * <p>桶名在此统一定义；连接信息见 {@code minio.*} 配置（后期可迁至 Nacos）</p>
 */
public final class MinioConstants {

    private MinioConstants() {
    }

    /** 文档图片、附件等资源 */
    public static final String BUCKET_WORKSPACE_ASSETS = "workspace-assets";

    /** 导出 docx/pdf 等临时文件（预留） */
    public static final String BUCKET_WORKSPACE_EXPORTS = "workspace-exports";

    /** 应用启动时需存在（不存在则自动创建）的桶 */
    public static final String[] REQUIRED_BUCKETS = {
            BUCKET_WORKSPACE_ASSETS,
            BUCKET_WORKSPACE_EXPORTS,
    };

    /** 文档资产对象 key 前缀：assets/{workspaceId}/{nodeId}/ */
    public static final String PATH_WORKSPACE_ASSETS = "assets";
}

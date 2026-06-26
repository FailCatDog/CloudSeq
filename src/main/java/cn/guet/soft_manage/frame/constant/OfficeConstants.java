package cn.guet.soft_manage.frame.constant;

/**
 * OnlyOffice 集成常量
 */
public final class OfficeConstants {

    public static final String DOWNLOAD_TOKEN_TYPE = "office-download";

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_NODE_ID = "nodeId";

    /** 文档正在编辑 */
    public static final int CALLBACK_STATUS_EDITING = 1;

    /** 文档待保存 */
    public static final int CALLBACK_STATUS_SAVE = 2;

    /** 保存出错 */
    public static final int CALLBACK_STATUS_SAVE_ERROR = 3;

    /** 关闭且无变更 */
    public static final int CALLBACK_STATUS_CLOSED = 4;

    /** 编辑中强制保存 */
    public static final int CALLBACK_STATUS_FORCE_SAVE = 6;

    /** 强制保存出错 */
    public static final int CALLBACK_STATUS_FORCE_SAVE_ERROR = 7;

    private OfficeConstants() {
    }

    public static String buildDocumentKey(Long nodeId, Long version) {
        return nodeId + "_" + version;
    }

    public static Long parseNodeIdFromKey(String key) {
        if (key == null || !key.contains("_")) {
            return null;
        }
        int idx = key.lastIndexOf('_');
        try {
            return Long.parseLong(key.substring(0, idx));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

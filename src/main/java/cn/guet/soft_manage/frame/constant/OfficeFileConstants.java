package cn.guet.soft_manage.frame.constant;

/**
 * Office 文件 MIME 与存储常量
 */
public final class OfficeFileConstants {

    public static final String DOCX_MIME =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public static final String DOCX_EXTENSION = ".docx";

    public static final String BLANK_DOCX_TEMPLATE = "templates/blank.docx";

    private OfficeFileConstants() {
    }

    public static String buildStorageKey(Long workspaceId, Long nodeId) {
        return "workspace/" + workspaceId + "/office/" + nodeId + DOCX_EXTENSION;
    }
}

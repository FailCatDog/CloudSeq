package cn.guet.soft_manage.biz.export.util;

/**
 * Detect common image MIME types from magic bytes when Content-Type is missing/wrong.
 */
public final class ImageMediaTypes {

    private ImageMediaTypes() {
    }

    public static String sniff(byte[] bytes, String declaredContentType) {
        String sniffed = sniffBytes(bytes);
        if (sniffed != null) {
            return sniffed;
        }
        return normalizeDeclared(declaredContentType);
    }

    public static String normalizeDeclared(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        String ct = contentType.toLowerCase().split(";")[0].trim();
        if (ct.contains("jpeg") || ct.contains("jpg")) {
            return "image/jpeg";
        }
        if (ct.contains("png")) {
            return "image/png";
        }
        if (ct.contains("gif")) {
            return "image/gif";
        }
        if (ct.contains("bmp")) {
            return "image/bmp";
        }
        // WebP / others: unsupported by POI DOCX picture API
        return null;
    }

    private static String sniffBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 3) {
            return null;
        }
        // PNG
        if (bytes.length >= 8
            && (bytes[0] & 0xFF) == 0x89
            && bytes[1] == 0x50
            && bytes[2] == 0x4E
            && bytes[3] == 0x47) {
            return "image/png";
        }
        // JPEG
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // GIF
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return "image/gif";
        }
        // BMP
        if (bytes[0] == 'B' && bytes[1] == 'M') {
            return "image/bmp";
        }
        return null;
    }
}

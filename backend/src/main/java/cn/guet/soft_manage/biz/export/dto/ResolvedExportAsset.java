package cn.guet.soft_manage.biz.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 已解析的内嵌图片资产
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolvedExportAsset {

    private byte[] bytes;

    private String contentType;

    private String fileName;
}

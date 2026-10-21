package cn.guet.soft_manage.biz.export.delivery;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import org.springframework.stereotype.Component;

/**
 * 同步直传：原样返回内存产物，由 Controller 写入响应体。
 */
@Component
public class SyncStreamDelivery implements ExportDelivery {

    @Override
    public ExportArtifact deliver(ExportArtifact artifact) {
        return artifact;
    }
}

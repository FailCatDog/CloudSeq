package cn.guet.soft_manage.biz.sheet.service;

import cn.guet.soft_manage.biz.document.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.sheet.dto.SheetDetailDTO;

/**
 * 工作区表格服务
 */
public interface SheetService {

    SheetDetailDTO getSheet(Long nodeId);

    CollabTokenResponseDTO issueCollabToken(Long nodeId);

    CollabLoadResponseDTO loadForCollab(Long nodeId);

    CollabPersistResponseDTO persistFromCollab(CollabPersistRequestDTO request);

    void deleteContentByNodeId(Long nodeId);
}

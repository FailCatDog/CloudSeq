package cn.guet.soft_manage.biz.sheet.service.impl;

import cn.guet.soft_manage.biz.sheet.service.SheetManageService;
import cn.guet.soft_manage.biz.sheet.service.SheetService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SheetManageServiceImpl implements SheetManageService {

    @Resource
    private SheetService sheetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanupByNodeId(Long nodeId) {
        if (nodeId == null) return;
        sheetService.deleteContentByNodeId(nodeId);
    }
}

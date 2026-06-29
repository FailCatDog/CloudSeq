package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.DictValueDTO;

import java.util.List;
import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典服务
 */
public interface DictService {

    List<DictValueDTO> listByKeyCode(String keyCode);

    Map<String, List<DictValueDTO>> listByKeyCodes(List<String> keyCodes);

    String getLabel(String keyCode, Object valueCode);

    void reloadCache();
}

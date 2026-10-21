package cn.guet.soft_manage.biz.dict.service;

import cn.guet.soft_manage.biz.dict.dto.DictValueDTO;

import java.util.List;
import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典服务
 */
public interface DictService {

    /**
     * 按字典类型查询字典项
     * @param keyCode 字典类型编码
     * @return 字典项列表
     */
    List<DictValueDTO> listByKeyCode(String keyCode);

    /**
     * 批量按字典类型查询字典项
     * @param keyCodes 字典类型编码列表
     * @return 字典项映射
     */
    Map<String, List<DictValueDTO>> listByKeyCodes(List<String> keyCodes);

    /**
     * 获取字典项显示文本
     * @param keyCode 字典类型编码
     * @param valueCode 字典值编码
     * @return 显示文本
     */
    String getLabel(String keyCode, Object valueCode);

    /**
     * 从数据库重建字典缓存
     */
    void reloadCache();
}

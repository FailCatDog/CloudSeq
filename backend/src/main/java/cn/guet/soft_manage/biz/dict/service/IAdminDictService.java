package cn.guet.soft_manage.biz.dict.service;

import cn.guet.soft_manage.biz.dict.dto.request.DictKeySaveRequestDTO;
import cn.guet.soft_manage.biz.dict.dto.request.DictValueSaveRequestDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictKeyDetailDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictKeySummaryDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictValueAdminDTO;

import java.util.List;

public interface IAdminDictService {

    List<DictKeySummaryDTO> listKeys();

    DictKeyDetailDTO getById(Long id);

    DictKeyDetailDTO createKey(DictKeySaveRequestDTO request);

    DictKeyDetailDTO updateKey(Long id, DictKeySaveRequestDTO request);

    void deleteKey(Long id);

    DictValueAdminDTO createValue(Long keyId, DictValueSaveRequestDTO request);

    DictValueAdminDTO updateValue(Long valueId, DictValueSaveRequestDTO request);

    void deleteValue(Long valueId);

    void reloadCache();
}

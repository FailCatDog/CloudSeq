package cn.guet.soft_manage.biz.dict.service.impl;

import cn.guet.soft_manage.biz.dict.dao.DictKeyDao;
import cn.guet.soft_manage.biz.dict.dao.DictValueDao;
import cn.guet.soft_manage.biz.dict.dto.request.DictKeySaveRequestDTO;
import cn.guet.soft_manage.biz.dict.dto.request.DictValueSaveRequestDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictKeyDetailDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictKeySummaryDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictValueAdminDTO;
import cn.guet.soft_manage.biz.dict.entity.DictKey;
import cn.guet.soft_manage.biz.dict.entity.DictValue;
import cn.guet.soft_manage.biz.dict.service.DictService;
import cn.guet.soft_manage.biz.dict.service.IAdminDictService;
import cn.guet.soft_manage.biz.rbac.support.AdminAuthSupport;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminDictServiceImpl implements IAdminDictService {

    @Resource
    private DictKeyDao dictKeyDao;

    @Resource
    private DictValueDao dictValueDao;

    @Resource
    private DictService dictService;

    @Resource
    private AdminAuthSupport adminAuthSupport;

    @Override
    public List<DictKeySummaryDTO> listKeys() {
        adminAuthSupport.requireAdmin();
        List<DictKey> keys = dictKeyDao.selectList(new LambdaQueryWrapper<DictKey>()
                .orderByAsc(DictKey::getKeyCode)
                .orderByAsc(DictKey::getDictKeyId));
        if (keys.isEmpty()) {
            return List.of();
        }
        List<Long> keyIds = keys.stream().map(DictKey::getDictKeyId).toList();
        Map<Long, Long> valueCountByKeyId = dictValueDao.selectList(new LambdaQueryWrapper<DictValue>()
                        .in(DictValue::getDictKeyId, keyIds))
                .stream()
                .collect(Collectors.groupingBy(DictValue::getDictKeyId, Collectors.counting()));
        return keys.stream()
                .map(key -> toSummary(key, valueCountByKeyId.getOrDefault(key.getDictKeyId(), 0L).intValue()))
                .toList();
    }

    @Override
    public DictKeyDetailDTO getById(Long id) {
        adminAuthSupport.requireAdmin();
        return toDetail(requireKey(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictKeyDetailDTO createKey(DictKeySaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        assertKeyCodeUnique(request.getKeyCode(), null);
        DictKey key = new DictKey();
        applyKeyRequest(key, request);
        dictKeyDao.insert(key);
        refreshCache();
        return toDetail(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictKeyDetailDTO updateKey(Long id, DictKeySaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        DictKey key = requireKey(id);
        if (request.getVersion() != null && !Objects.equals(key.getVersion(), request.getVersion())) {
            throw new BusinessException(BizResponseCode.DICT_KEY_STALE);
        }
        assertKeyCodeUnique(request.getKeyCode(), id);
        applyKeyRequest(key, request);
        dictKeyDao.updateById(key);
        refreshCache();
        return toDetail(requireKey(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKey(Long id) {
        adminAuthSupport.requireAdmin();
        requireKey(id);
        dictValueDao.delete(new LambdaQueryWrapper<DictValue>()
                .eq(DictValue::getDictKeyId, id));
        dictKeyDao.deleteById(id);
        refreshCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictValueAdminDTO createValue(Long keyId, DictValueSaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        requireKey(keyId);
        assertValueCodeUnique(keyId, request.getValueCode(), null);
        DictValue value = new DictValue();
        value.setDictKeyId(keyId);
        applyValueRequest(value, request);
        if (value.getSort() == null) {
            value.setSort(0);
        }
        dictValueDao.insert(value);
        refreshCache();
        return toValueDto(value);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictValueAdminDTO updateValue(Long valueId, DictValueSaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        DictValue value = requireValue(valueId);
        if (request.getVersion() != null && !Objects.equals(value.getVersion(), request.getVersion())) {
            throw new BusinessException(BizResponseCode.DICT_VALUE_STALE);
        }
        assertValueCodeUnique(value.getDictKeyId(), request.getValueCode(), valueId);
        applyValueRequest(value, request);
        dictValueDao.updateById(value);
        refreshCache();
        return toValueDto(requireValue(valueId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteValue(Long valueId) {
        adminAuthSupport.requireAdmin();
        requireValue(valueId);
        dictValueDao.deleteById(valueId);
        refreshCache();
    }

    @Override
    public void reloadCache() {
        adminAuthSupport.requireAdmin();
        refreshCache();
    }

    private void refreshCache() {
        dictService.reloadCache();
    }

    private DictKey requireKey(Long id) {
        DictKey key = dictKeyDao.selectById(id);
        if (key == null) {
            throw new BusinessException(BizResponseCode.DICT_KEY_NOT_FOUND);
        }
        return key;
    }

    private DictValue requireValue(Long id) {
        DictValue value = dictValueDao.selectById(id);
        if (value == null) {
            throw new BusinessException(BizResponseCode.DICT_VALUE_NOT_FOUND);
        }
        return value;
    }

    private void assertKeyCodeUnique(String keyCode, Long excludeId) {
        if (!StringUtils.hasText(keyCode)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<DictKey> wrapper = new LambdaQueryWrapper<DictKey>()
                .eq(DictKey::getKeyCode, keyCode.trim());
        if (excludeId != null) {
            wrapper.ne(DictKey::getDictKeyId, excludeId);
        }
        if (dictKeyDao.exists(wrapper)) {
            throw new BusinessException(BizResponseCode.DICT_KEY_EXISTS);
        }
    }

    private void assertValueCodeUnique(Long keyId, String valueCode, Long excludeId) {
        if (!StringUtils.hasText(valueCode)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<DictValue> wrapper = new LambdaQueryWrapper<DictValue>()
                .eq(DictValue::getDictKeyId, keyId)
                .eq(DictValue::getValueCode, valueCode.trim());
        if (excludeId != null) {
            wrapper.ne(DictValue::getDictValueId, excludeId);
        }
        if (dictValueDao.exists(wrapper)) {
            throw new BusinessException(BizResponseCode.DICT_VALUE_EXISTS);
        }
    }

    private void applyKeyRequest(DictKey key, DictKeySaveRequestDTO request) {
        key.setKeyCode(request.getKeyCode().trim());
        key.setKeyName(request.getKeyName().trim());
        key.setRemark(trimToNull(request.getRemark()));
    }

    private void applyValueRequest(DictValue value, DictValueSaveRequestDTO request) {
        value.setValueCode(request.getValueCode().trim());
        value.setValueName(request.getValueName().trim());
        value.setRemark(trimToNull(request.getRemark()));
        value.setSort(request.getSort());
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private DictKeyDetailDTO toDetail(DictKey key) {
        List<DictValue> values = dictValueDao.selectList(new LambdaQueryWrapper<DictValue>()
                .eq(DictValue::getDictKeyId, key.getDictKeyId())
                .orderByAsc(DictValue::getSort)
                .orderByAsc(DictValue::getDictValueId));
        List<DictValueAdminDTO> valueDtos = values.stream()
                .sorted(Comparator.comparing(DictValue::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DictValue::getValueCode))
                .map(this::toValueDto)
                .toList();
        return DictKeyDetailDTO.builder()
                .id(key.getDictKeyId())
                .keyCode(key.getKeyCode())
                .keyName(key.getKeyName())
                .remark(key.getRemark())
                .version(key.getVersion())
                .values(valueDtos)
                .build();
    }

    private DictKeySummaryDTO toSummary(DictKey key, int valueCount) {
        return DictKeySummaryDTO.builder()
                .id(key.getDictKeyId())
                .keyCode(key.getKeyCode())
                .keyName(key.getKeyName())
                .remark(key.getRemark())
                .valueCount(valueCount)
                .version(key.getVersion())
                .build();
    }

    private DictValueAdminDTO toValueDto(DictValue value) {
        return DictValueAdminDTO.builder()
                .id(value.getDictValueId())
                .dictKeyId(value.getDictKeyId())
                .valueCode(value.getValueCode())
                .valueName(value.getValueName())
                .remark(value.getRemark())
                .sort(value.getSort())
                .version(value.getVersion())
                .build();
    }
}

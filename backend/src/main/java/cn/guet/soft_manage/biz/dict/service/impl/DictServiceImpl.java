package cn.guet.soft_manage.biz.dict.service.impl;

import cn.guet.soft_manage.biz.dict.dao.DictKeyDao;
import cn.guet.soft_manage.biz.dict.dao.DictValueDao;
import cn.guet.soft_manage.biz.dict.dto.DictValueDTO;
import cn.guet.soft_manage.biz.dict.entity.DictKey;
import cn.guet.soft_manage.biz.dict.entity.DictValue;
import cn.guet.soft_manage.biz.dict.service.DictService;
import cn.guet.soft_manage.biz.common.util.RedissonUtil;
import cn.guet.soft_manage.frame.config.DictCacheProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典服务实现（DB + Redisson Hash + 本地内存）
 */
@Service
public class DictServiceImpl implements DictService {

    private static final TypeReference<List<DictValueDTO>> DICT_VALUE_LIST_TYPE = new TypeReference<>() {};

    @Resource
    private DictKeyDao dictKeyDao;

    @Resource
    private DictValueDao dictValueDao;

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private DictCacheProperties dictCacheProperties;

    @Resource
    private ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, List<DictValueDTO>> cacheByKeyCode = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        try {
            if (loadFromRedis()) return;
        } catch (Exception ignored) {
            // Redis 不可用时回源 DB
        }
        reloadCache();
    }

    @Override
    public void reloadCache() {
        Map<String, List<DictValueDTO>> grouped = loadFromDatabase();
        writeToRedis(grouped);
        cacheByKeyCode.clear();
        cacheByKeyCode.putAll(grouped);
    }

    @Override
    public List<DictValueDTO> listByKeyCode(String keyCode) {
        if (!StringUtils.hasText(keyCode)) throw new BusinessException(BizResponseCode.DICT_KEY_NOT_FOUND);
        String code = keyCode.trim();
        List<DictValueDTO> values = resolveValues(code);
        if (values == null) throw new BusinessException(BizResponseCode.DICT_KEY_NOT_FOUND);
        return values;
    }

    @Override
    public Map<String, List<DictValueDTO>> listByKeyCodes(List<String> keyCodes) {
        if (keyCodes == null || keyCodes.isEmpty()) return Map.of();
        Map<String, List<DictValueDTO>> result = new LinkedHashMap<>();
        for (String keyCode : keyCodes) {
            if (!StringUtils.hasText(keyCode)) continue;
            String code = keyCode.trim();
            List<DictValueDTO> values = resolveValues(code);
            if (values == null) throw new BusinessException(BizResponseCode.DICT_KEY_NOT_FOUND);
            result.put(code, values);
        }
        return result;
    }

    @Override
    public String getLabel(String keyCode, Object valueCode) {
        if (!StringUtils.hasText(keyCode) || valueCode == null) return null;
        List<DictValueDTO> values = resolveValues(keyCode.trim());
        if (values == null) return null;
        String code = String.valueOf(valueCode);
        for (DictValueDTO item : values) {
            if (Objects.equals(item.getValueCode(), code)) return item.getValueName();
        }
        return null;
    }

    private List<DictValueDTO> resolveValues(String keyCode) {
        List<DictValueDTO> cached = cacheByKeyCode.get(keyCode);
        if (cached != null) return cached;

        List<DictValueDTO> fromRedis = readKeyFromRedis(keyCode);
        if (fromRedis != null) {
            cacheByKeyCode.put(keyCode, fromRedis);
            return fromRedis;
        }

        List<DictValueDTO> fromDb = loadOneFromDatabase(keyCode);
        if (fromDb == null) return null;

        putOneToCache(keyCode, fromDb);
        return fromDb;
    }

    private boolean loadFromRedis() {
        String redisKey = dictCacheProperties.getRedisKey();
        Map<String, String> entries = redissonUtil.hashGetAll(redisKey);
        if (entries.isEmpty()) return false;

        Map<String, List<DictValueDTO>> grouped = parseRedisHash(entries);
        if (grouped.isEmpty()) return false;

        cacheByKeyCode.clear();
        cacheByKeyCode.putAll(grouped);
        return true;
    }

    private List<DictValueDTO> loadOneFromDatabase(String keyCode) {
        DictKey key = dictKeyDao.selectOne(new LambdaQueryWrapper<DictKey>()
                .eq(DictKey::getKeyCode, keyCode)
                .last("LIMIT 1"));
        if (key == null) return null;

        List<DictValue> values = dictValueDao.selectList(new LambdaQueryWrapper<DictValue>()
                .eq(DictValue::getDictKeyId, key.getDictKeyId())
                .orderByAsc(DictValue::getSort)
                .orderByAsc(DictValue::getDictValueId));

        List<DictValueDTO> dtos = values.stream().map(this::toDto).collect(Collectors.toCollection(ArrayList::new));
        dtos.sort(Comparator.comparing(DictValueDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DictValueDTO::getValueCode));
        return List.copyOf(dtos);
    }

    private void putOneToCache(String keyCode, List<DictValueDTO> values) {
        cacheByKeyCode.put(keyCode, values);
        try {
            String redisKey = dictCacheProperties.getRedisKey();
            redissonUtil.hashSet(redisKey, keyCode, objectMapper.writeValueAsString(values));
            redissonUtil.expire(redisKey, Duration.ofDays(dictCacheProperties.getTtlDays()));
        } catch (Exception ignored) {
            // Redis 不可用时仅保留本地缓存
        }
    }

    private Map<String, List<DictValueDTO>> loadFromDatabase() {
        List<DictKey> keys = dictKeyDao.selectList(new LambdaQueryWrapper<DictKey>()
                .orderByAsc(DictKey::getDictKeyId));
        if (keys.isEmpty()) return Map.of();

        List<Long> keyIds = keys.stream().map(DictKey::getDictKeyId).toList();
        List<DictValue> values = dictValueDao.selectList(new LambdaQueryWrapper<DictValue>()
                .in(DictValue::getDictKeyId, keyIds)
                .orderByAsc(DictValue::getSort)
                .orderByAsc(DictValue::getDictValueId));

        Map<Long, String> keyIdToCode = keys.stream()
                .collect(Collectors.toMap(DictKey::getDictKeyId, DictKey::getKeyCode));

        Map<String, List<DictValueDTO>> grouped = new LinkedHashMap<>();
        for (DictKey key : keys) grouped.put(key.getKeyCode(), new ArrayList<>());

        for (DictValue value : values) {
            String code = keyIdToCode.get(value.getDictKeyId());
            if (code == null) continue;
            grouped.computeIfAbsent(code, ignored -> new ArrayList<>()).add(toDto(value));
        }

        for (List<DictValueDTO> list : grouped.values()) {
            list.sort(Comparator.comparing(DictValueDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(DictValueDTO::getValueCode));
        }
        return grouped;
    }

    private void writeToRedis(Map<String, List<DictValueDTO>> grouped) {
        if (grouped.isEmpty()) return;

        String redisKey = dictCacheProperties.getRedisKey();
        Map<String, String> hash = new LinkedHashMap<>();
        for (Map.Entry<String, List<DictValueDTO>> entry : grouped.entrySet()) {
            try {
                hash.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
            } catch (Exception ex) {
                throw new BusinessException(BizResponseCode.SYSTEM_ERROR);
            }
        }

        redissonUtil.hashReplaceAll(redisKey, hash, Duration.ofDays(dictCacheProperties.getTtlDays()));
    }

    private List<DictValueDTO> readKeyFromRedis(String keyCode) {
        String redisKey = dictCacheProperties.getRedisKey();
        String raw = redissonUtil.hashGet(redisKey, keyCode);
        if (raw == null) return null;
        try {
            List<DictValueDTO> values = objectMapper.readValue(raw, DICT_VALUE_LIST_TYPE);
            return values == null ? null : List.copyOf(values);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, List<DictValueDTO>> parseRedisHash(Map<String, String> entries) {
        Map<String, List<DictValueDTO>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            try {
                List<DictValueDTO> values = objectMapper.readValue(entry.getValue(), DICT_VALUE_LIST_TYPE);
                if (values != null) grouped.put(entry.getKey(), List.copyOf(values));
            } catch (Exception ignored) {
                // 跳过损坏 field
            }
        }
        return grouped;
    }

    private DictValueDTO toDto(DictValue value) {
        return DictValueDTO.builder()
                .valueCode(value.getValueCode())
                .valueName(value.getValueName())
                .remark(value.getRemark())
                .sort(value.getSort())
                .build();
    }
}

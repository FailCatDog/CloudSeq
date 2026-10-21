package cn.guet.soft_manage.biz.util;

import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: Redisson 常用操作封装
 */
@Component
public class RedissonUtil {

    @Resource
    private RedissonClient redissonClient;

    public boolean exists(String key) {
        return redissonClient.getKeys().countExists(key) > 0;
    }

    public boolean delete(String key) {
        return redissonClient.getKeys().delete(key) > 0;
    }

    public boolean expire(String key, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) return false;
        return redissonClient.getKeys().expire(key, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void set(String key, String value) {
        redissonClient.<String>getBucket(key).set(value);
    }

    public void set(String key, String value, Duration ttl) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            bucket.set(value);
            return;
        }
        bucket.set(value, ttl);
    }

    public String get(String key) {
        return redissonClient.<String>getBucket(key).get();
    }

    public void hashSet(String key, String field, String value) {
        redissonClient.<String, String>getMap(key).put(field, value);
    }

    public String hashGet(String key, String field) {
        return redissonClient.<String, String>getMap(key).get(field);
    }

    public Map<String, String> hashGetAll(String key) {
        RMap<String, String> map = redissonClient.getMap(key);
        if (!map.isExists()) return Collections.emptyMap();
        return new LinkedHashMap<>(map.readAllMap());
    }

    public void hashPutAll(String key, Map<String, String> entries) {
        if (entries == null || entries.isEmpty()) return;
        redissonClient.<String, String>getMap(key).putAll(entries);
    }

    /** 全量替换 Hash 并设置 TTL */
    public void hashReplaceAll(String key, Map<String, String> entries, Duration ttl) {
        RMap<String, String> map = redissonClient.getMap(key);
        map.delete();
        if (entries != null && !entries.isEmpty()) {
            map.putAll(entries);
            if (ttl != null && !ttl.isZero() && !ttl.isNegative()) map.expire(ttl);
        }
    }
}

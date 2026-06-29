package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典 Redis 缓存配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "dict.cache")
public class DictCacheProperties {

    /** Redis Hash Key，field 为 dict key_code，value 为该类型全部字典项 JSON 数组 */
    private String redisKey = "sm:dict";

    /** 缓存 TTL（天） */
    private long ttlDays = 7;
}

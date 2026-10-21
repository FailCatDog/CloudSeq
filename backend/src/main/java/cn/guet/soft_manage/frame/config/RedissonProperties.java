package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: Redisson 连接配置，读取 spring.data.redis
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonProperties {

    private String host = "127.0.0.1";

    private int port = 6379;

    private int database = 0;

    private String password;
}

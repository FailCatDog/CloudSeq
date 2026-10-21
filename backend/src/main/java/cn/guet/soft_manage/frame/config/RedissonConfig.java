package cn.guet.soft_manage.frame.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: Redisson 客户端配置
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedissonProperties properties) {
        Config config = new Config();
        var server = config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setDatabase(properties.getDatabase());
        if (StringUtils.hasText(properties.getPassword())) server.setPassword(properties.getPassword());
        return Redisson.create(config);
    }
}

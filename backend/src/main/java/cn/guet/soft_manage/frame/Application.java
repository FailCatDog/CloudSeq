package cn.guet.soft_manage.frame;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 应用启动类
 */
@SpringBootApplication(scanBasePackages = "cn.guet.soft_manage")
@MapperScan("cn.guet.soft_manage.biz")
public class Application {

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }
}

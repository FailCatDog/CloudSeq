package cn.guet.feishu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.guet.feishu.mapper")
public class FeiShuApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeiShuApplication.class, args);
    }
}


package cn.guet.soft_manage.frame.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: MyBatis-Plus 自动填充处理器
 *
 * 说明：当前先使用占位用户ID 0L，后续登录接入后改为从 JWT/登录上下文获取。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final Long SYSTEM_USER_ID = 0L;

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "create_user", Long.class, SYSTEM_USER_ID);
        this.strictInsertFill(metaObject, "create_date", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "update_user", Long.class, SYSTEM_USER_ID);
        this.strictInsertFill(metaObject, "update_date", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "update_user", Long.class, SYSTEM_USER_ID);
        this.strictUpdateFill(metaObject, "update_date", LocalDateTime.class, LocalDateTime.now());
    }
}

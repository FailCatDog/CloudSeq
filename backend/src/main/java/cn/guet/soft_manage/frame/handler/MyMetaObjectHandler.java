package cn.guet.soft_manage.frame.handler;

import cn.guet.soft_manage.frame.auth.UserContext;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: MyBatis-Plus 自动填充处理器
 *
 * 登录态取当前用户；注册等无 Token 场景回退系统用户 1，避免 create_user 写入 null
 * （MP 显式 INSERT 字段时不会走列 DEFAULT）。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /** 与表结构 DEFAULT 1、种子账号 admin 对齐 */
    private static final Long SYSTEM_USER_ID = 1L;

    @Override
    public void insertFill(MetaObject metaObject) {
        Long operatorId = resolveOperatorId();
        this.strictInsertFill(metaObject, "createUser", Long.class, operatorId);
        this.strictInsertFill(metaObject, "createDate", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateUser", Long.class, operatorId);
        this.strictInsertFill(metaObject, "updateDate", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long operatorId = resolveOperatorId();
        this.strictUpdateFill(metaObject, "updateUser", Long.class, operatorId);
        this.strictUpdateFill(metaObject, "updateDate", LocalDateTime.class, LocalDateTime.now());
    }

    private Long resolveOperatorId() {
        Long userId = UserContext.getUserId();
        return userId != null ? userId : SYSTEM_USER_ID;
    }
}

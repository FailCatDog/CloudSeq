package cn.guet.soft_manage.frame.common;

import cn.guet.soft_manage.biz.pojo.entity.User;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 当前用户上下文
 */
public final class UserContext {

    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(User User) {
        USER_HOLDER.set(User);
    }

    public static User get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        User user = USER_HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}

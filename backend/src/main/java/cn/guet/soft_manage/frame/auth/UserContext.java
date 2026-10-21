package cn.guet.soft_manage.frame.auth;

/**
 * 当前登录用户上下文
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = USER_HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}

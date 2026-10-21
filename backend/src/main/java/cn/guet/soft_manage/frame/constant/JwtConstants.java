package cn.guet.soft_manage.frame.constant;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: JWT 常量
 */
public final class JwtConstants {

    private JwtConstants() {
    }

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_ID = "id";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_STUDENT_NO = "studentNo";
    public static final String CLAIM_NICK_NAME = "nickName";
    public static final String CLAIM_REAL_NAME = "realName";
    public static final String CLAIM_AVATAR_URL = "avatarUrl";
    public static final long EXPIRE_SECONDS = 24L * 60L * 60L;
    public static final String SECRET = "soft_manage-jwt-secret-key-change-me";
}

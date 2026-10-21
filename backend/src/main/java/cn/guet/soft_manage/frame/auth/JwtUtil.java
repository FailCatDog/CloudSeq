package cn.guet.soft_manage.frame.auth;

import cn.guet.soft_manage.frame.constant.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 签发与解析
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    public static String generateToken(LoginUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIM_ID, user.getId());
        claims.put(JwtConstants.CLAIM_USERNAME, user.getUsername());
        claims.put(JwtConstants.CLAIM_ROLE, user.getRole());
        claims.put(JwtConstants.CLAIM_STUDENT_NO, user.getStudentNo());
        putIfHasText(claims, JwtConstants.CLAIM_NICK_NAME, user.getNickName());
        putIfHasText(claims, JwtConstants.CLAIM_REAL_NAME, user.getRealName());
        putIfHasText(claims, JwtConstants.CLAIM_AVATAR_URL, user.getAvatarUrl());

        Date now = new Date();
        Date expireAt = new Date(now.getTime() + JwtConstants.EXPIRE_SECONDS * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static LoginUser parseLoginUser(String token) {
        Claims claims = parseToken(token);
        return LoginUser.builder()
                .id(claims.get(JwtConstants.CLAIM_ID, Long.class))
                .username(claims.get(JwtConstants.CLAIM_USERNAME, String.class))
                .role(claims.get(JwtConstants.CLAIM_ROLE, String.class))
                .studentNo(claims.get(JwtConstants.CLAIM_STUDENT_NO, String.class))
                .nickName(claims.get(JwtConstants.CLAIM_NICK_NAME, String.class))
                .realName(claims.get(JwtConstants.CLAIM_REAL_NAME, String.class))
                .avatarUrl(claims.get(JwtConstants.CLAIM_AVATAR_URL, String.class))
                .build();
    }

    private static void putIfHasText(Map<String, Object> claims, String key, String value) {
        if (StringUtils.hasText(value)) {
            claims.put(key, value);
        }
    }

    private static SecretKey getSecretKey() {
        byte[] keyBytes = JwtConstants.SECRET.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

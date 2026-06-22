package cn.guet.soft_manage.biz.utils;

import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.frame.constant.JwtConstants;
import cn.guet.soft_manage.frame.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: JWT 工具类
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIM_ID, user.getId());
        claims.put(JwtConstants.CLAIM_USERNAME, user.getUsername());
        claims.put(JwtConstants.CLAIM_ROLE, UserRole.getRoleName(user.getRole()));
        claims.put(JwtConstants.CLAIM_STUDENT_NO, user.getStudentNo());

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
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw ex;
        }
    }

    public static User getLoginUser(String token) {
        Claims claims = parseToken(token);
        return User.builder()
                .id(claims.get(JwtConstants.CLAIM_ID, Long.class))
                .username(claims.get(JwtConstants.CLAIM_USERNAME, String.class))
                .role(UserRole.getRoleCode(claims.get(JwtConstants.CLAIM_ROLE, String.class)))
                .studentNo(claims.get(JwtConstants.CLAIM_STUDENT_NO, String.class))
                .build();
    }

    private static SecretKey getSecretKey() {
        byte[] keyBytes = JwtConstants.SECRET.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

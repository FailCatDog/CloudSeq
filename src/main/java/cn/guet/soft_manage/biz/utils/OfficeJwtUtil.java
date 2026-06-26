package cn.guet.soft_manage.biz.utils;

import cn.guet.soft_manage.frame.config.OfficeProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Map;

/**
 * OnlyOffice JWT 签名与解析
 */
@Component
public class OfficeJwtUtil {

    @Resource
    private OfficeProperties officeProperties;

    public String signPayload(Map<String, Object> payload) {
        return Jwts.builder()
                .claims(payload)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(officeProperties.getJwtSecret().getBytes());
    }
}

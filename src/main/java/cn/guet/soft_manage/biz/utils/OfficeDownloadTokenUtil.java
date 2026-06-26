package cn.guet.soft_manage.biz.utils;

import cn.guet.soft_manage.frame.config.OfficeProperties;
import cn.guet.soft_manage.frame.constant.OfficeConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OnlyOffice 文档下载短期令牌
 */
@Component
public class OfficeDownloadTokenUtil {

    @Resource
    private OfficeProperties officeProperties;

    public String generateToken(Long nodeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(OfficeConstants.CLAIM_TYPE, OfficeConstants.DOWNLOAD_TOKEN_TYPE);
        claims.put(OfficeConstants.CLAIM_NODE_ID, nodeId);

        Date now = new Date();
        Date expireAt = new Date(now.getTime() + officeProperties.getDownloadTokenExpireSeconds() * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Long parseNodeId(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(BizResponseCode.OFFICE_TOKEN_INVALID);
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.OFFICE_TOKEN_INVALID);
        }

        if (!Objects.equals(OfficeConstants.DOWNLOAD_TOKEN_TYPE, claims.get(OfficeConstants.CLAIM_TYPE, String.class))) {
            throw new BusinessException(BizResponseCode.OFFICE_TOKEN_INVALID);
        }

        Long nodeId = claims.get(OfficeConstants.CLAIM_NODE_ID, Long.class);
        if (nodeId == null) {
            throw new BusinessException(BizResponseCode.OFFICE_TOKEN_INVALID);
        }
        return nodeId;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(officeProperties.getJwtSecret().getBytes());
    }
}

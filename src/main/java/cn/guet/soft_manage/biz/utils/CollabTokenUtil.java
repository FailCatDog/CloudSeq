package cn.guet.soft_manage.biz.utils;

import cn.guet.soft_manage.biz.pojo.dto.CollabTokenClaimsDTO;
import cn.guet.soft_manage.frame.config.CollabProperties;
import cn.guet.soft_manage.frame.constant.CollabConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
 * 协同会话 JWT 工具
 */
@Component
public class CollabTokenUtil {

    @Resource
    private CollabProperties collabProperties;

    public String generateToken(Long userId, Long nodeId, boolean canWrite, String displayName, String avatarUrl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CollabConstants.CLAIM_TYPE, CollabConstants.TOKEN_TYPE);
        // 以字符串写入，避免 Node 端 jsonwebtoken 解析 JSON 数字时丢失雪花 ID 精度
        claims.put(CollabConstants.CLAIM_USER_ID, String.valueOf(userId));
        claims.put(CollabConstants.CLAIM_NODE_ID, String.valueOf(nodeId));
        claims.put(CollabConstants.CLAIM_CAN_WRITE, canWrite);
        claims.put(CollabConstants.CLAIM_DISPLAY_NAME, displayName);
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            claims.put(CollabConstants.CLAIM_AVATAR_URL, avatarUrl);
        }

        Date now = new Date();
        Date expireAt = new Date(now.getTime() + collabProperties.getTokenExpireSeconds() * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public CollabTokenClaimsDTO parseToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(BizResponseCode.COLLAB_TOKEN_EXPIRED);
        } catch (JwtException ex) {
            throw new BusinessException(BizResponseCode.COLLAB_TOKEN_INVALID);
        }

        if (!Objects.equals(CollabConstants.TOKEN_TYPE, claims.get(CollabConstants.CLAIM_TYPE, String.class))) {
            throw new BusinessException(BizResponseCode.COLLAB_TOKEN_INVALID);
        }

        String userIdText = claims.get(CollabConstants.CLAIM_USER_ID, String.class);
        String nodeIdText = claims.get(CollabConstants.CLAIM_NODE_ID, String.class);
        if (userIdText == null || nodeIdText == null) {
            throw new BusinessException(BizResponseCode.COLLAB_TOKEN_INVALID);
        }

        Long userId;
        Long nodeId;
        try {
            userId = Long.parseLong(userIdText);
            nodeId = Long.parseLong(nodeIdText);
        } catch (NumberFormatException ex) {
            throw new BusinessException(BizResponseCode.COLLAB_TOKEN_INVALID);
        }

        return CollabTokenClaimsDTO.builder()
                .userId(userId)
                .nodeId(nodeId)
                .canWrite(Boolean.TRUE.equals(claims.get(CollabConstants.CLAIM_CAN_WRITE, Boolean.class)))
                .displayName(claims.get(CollabConstants.CLAIM_DISPLAY_NAME, String.class))
                .avatarUrl(claims.get(CollabConstants.CLAIM_AVATAR_URL, String.class))
                .build();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(collabProperties.getJwtSecret().getBytes());
    }
}

package com.buildflow.erp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析、验证功能
 *
 * <p>jjwt 0.12.x API 升级说明：
 * <ul>
 *   <li>使用 Jwts.builder().signWith(SecretKey) 替代 signWith(SignatureAlgorithm, String)</li>
 *   <li>使用 Jwts.parser().verifyWith(SecretKey).build() 替代 parser().setSigningKey(String)</li>
 *   <li>密钥使用 Keys.hmacShaKeyFor() 构建，要求密钥长度 >= 256 位（32字节）</li>
 * </ul></p>
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 构建HMAC-SHA签名密钥
     * @description 将字符串密钥转换为符合jjwt 0.12.x要求的SecretKey对象
     * @return SecretKey HMAC-SHA密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param roleId 角色ID
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, String username, Long roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleId", roleId);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从令牌中获取角色ID
     * @param token JWT令牌
     * @return 角色ID
     */
    public Long getRoleIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("roleId").toString());
    }

    /**
     * 验证令牌是否有效
     * @param token JWT令牌
     * @return 有效返回true，过期或无效返回false
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中解析Claims
     * @param token JWT令牌
     * @return Claims对象
     * @throws io.jsonwebtoken.ExpiredJwtException 令牌过期时抛出
     * @throws io.jsonwebtoken.security.SignatureException 签名不匹配时抛出
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

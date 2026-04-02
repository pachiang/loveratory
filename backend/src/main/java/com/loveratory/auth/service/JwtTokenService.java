package com.loveratory.auth.service;

import com.loveratory.auth.entity.UserRole;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token 服務。
 * 負責產生、解析與驗證 JWT Access Token 與 Refresh Token。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    /**
     * 產生 Access Token。
     * 包含使用者 ID、Email、角色等 Claims。
     *
     * @param userId 使用者 ID
     * @param email  使用者 Email
     * @param role   使用者角色
     * @return JWT Access Token 字串
     */
    public String generateAccessToken(@NonNull UUID userId,
                                      @NonNull String email,
                                      @NonNull UserRole role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpiration().toMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 產生 Refresh Token。
     * 僅包含使用者 ID 與 Token 類型。
     *
     * @param userId 使用者 ID
     * @return JWT Refresh Token 字串
     */
    public String generateRefreshToken(@NonNull UUID userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshTokenExpiration().toMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析並驗證 Token。
     * Token 無效或過期時拋出 BusinessException。
     *
     * @param token JWT Token 字串
     * @return 解析後的 Claims
     */
    public Claims parseToken(@NonNull String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            log.warn("Token 已過期");
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException exception) {
            log.warn("無效的 Token: {}", exception.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 從 Token 中取得使用者 ID。
     *
     * @param token JWT Token 字串
     * @return 使用者 ID
     */
    public UUID getUserIdFromToken(@NonNull String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * 驗證 Token 是否有效。
     *
     * @param token JWT Token 字串
     * @return 是否有效
     */
    public boolean isTokenValid(@NonNull String token) {
        try {
            parseToken(token);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    /**
     * 取得 HMAC-SHA256 簽章金鑰。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }
}

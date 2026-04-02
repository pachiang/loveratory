package com.loveratory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT 設定屬性。
 * 從 application.yml 中的 app.jwt 前綴讀取 JWT 相關設定。
 *
 * @param secret              簽章金鑰
 * @param accessTokenExpiration  Access Token 有效時間
 * @param refreshTokenExpiration Refresh Token 有效時間
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}

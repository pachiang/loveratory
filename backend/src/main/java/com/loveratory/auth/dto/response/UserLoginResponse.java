package com.loveratory.auth.dto.response;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * 登入回應。
 * 包含 Access Token、Refresh Token 與使用者基本資料。
 */
@Getter
@Builder
@Schema(description = "登入回應")
public class UserLoginResponse {

    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String accessToken;

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String refreshToken;

    @Schema(description = "使用者 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID userId;

    @Schema(description = "使用者 Email", example = "user@example.com")
    private final String email;

    @Schema(description = "使用者姓名", example = "王小明")
    private final String name;

    @Schema(description = "使用者角色", example = "USER")
    private final UserRole role;

    /**
     * 從 UserEntity 與 Token 建立登入回應。
     *
     * @param entity       使用者 Entity
     * @param accessToken  Access Token
     * @param refreshToken Refresh Token
     * @return 登入回應
     */
    public static UserLoginResponse fromEntity(@NonNull UserEntity entity,
                                               @NonNull String accessToken,
                                               @NonNull String refreshToken) {
        return UserLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .role(entity.getRole())
                .build();
    }
}

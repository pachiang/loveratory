package com.loveratory.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Token 刷新請求。
 */
@Getter
@Setter
@Schema(description = "Token 刷新請求")
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh Token 不可為空")
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}

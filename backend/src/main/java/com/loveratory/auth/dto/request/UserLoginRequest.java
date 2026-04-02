package com.loveratory.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 使用者登入請求。
 */
@Getter
@Setter
@Schema(description = "使用者登入請求")
public class UserLoginRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "使用者 Email", example = "user@example.com")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Schema(description = "密碼", example = "password123")
    private String password;
}

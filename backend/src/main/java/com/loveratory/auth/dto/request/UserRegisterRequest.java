package com.loveratory.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 使用者註冊請求。
 */
@Getter
@Setter
@Schema(description = "使用者註冊請求")
public class UserRegisterRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "使用者 Email", example = "user@example.com")
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 100, message = "密碼長度需介於 8 至 100 字元")
    @Schema(description = "密碼", example = "password123")
    private String password;

    @NotBlank(message = "姓名不可為空")
    @Size(max = 100, message = "姓名長度不可超過 100 字元")
    @Schema(description = "使用者姓名", example = "王小明")
    private String name;
}

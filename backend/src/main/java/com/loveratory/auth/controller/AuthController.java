package com.loveratory.auth.controller;

import com.loveratory.auth.dto.request.BootstrapAdminRequest;
import com.loveratory.auth.dto.request.TokenRefreshRequest;
import com.loveratory.auth.dto.request.UserLoginRequest;
import com.loveratory.auth.dto.request.UserRegisterRequest;
import com.loveratory.auth.dto.response.UserLoginResponse;
import com.loveratory.auth.usecase.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認證管理 API。
 * 提供使用者註冊、登入與 Token 刷新功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "認證管理", description = "使用者註冊、登入與 Token 管理")
public class AuthController {

    private final AuthUseCase authUseCase;

    /**
     * 使用者註冊。
     *
     * @param request 註冊請求
     * @return 登入回應（含 Token 與使用者資料）
     */
    @Operation(summary = "使用者註冊", description = "註冊新使用者帳號，成功後回傳 Token")
    @PostMapping("/register")
    public UserLoginResponse register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("使用者註冊，email: {}", request.getEmail());
        return authUseCase.register(request);
    }

    /**
     * 使用者登入。
     *
     * @param request 登入請求
     * @return 登入回應（含 Token 與使用者資料）
     */
    @Operation(summary = "使用者登入", description = "使用 Email 與密碼登入，成功後回傳 Token")
    @PostMapping("/login")
    public UserLoginResponse login(@Valid @RequestBody UserLoginRequest request) {
        log.info("使用者登入，email: {}", request.getEmail());
        return authUseCase.login(request);
    }

    /**
     * 刷新 Token。
     *
     * @param request Token 刷新請求
     * @return 登入回應（含新的 Token 與使用者資料）
     */
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 取得新的 Token 組")
    @PostMapping("/refresh")
    public UserLoginResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("刷新 Token");
        return authUseCase.refreshToken(request);
    }

    @Operation(summary = "Bootstrap system admin", description = "Create the first SYSTEM_ADMIN account when bootstrap is enabled")
    @PostMapping("/bootstrap-admin")
    public UserLoginResponse bootstrapAdmin(@Valid @RequestBody BootstrapAdminRequest request) {
        log.info("Bootstrap system admin, email: {}", request.getEmail());
        return authUseCase.bootstrapAdmin(request);
    }
}

package com.loveratory.auth.usecase;

import com.loveratory.auth.dto.request.BootstrapAdminRequest;
import com.loveratory.auth.dto.request.TokenRefreshRequest;
import com.loveratory.auth.dto.request.UserLoginRequest;
import com.loveratory.auth.dto.request.UserRegisterRequest;
import com.loveratory.auth.dto.response.UserLoginResponse;
import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.entity.UserRole;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.auth.service.AuthenticationService;
import com.loveratory.auth.service.JwtTokenService;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.config.BootstrapAdminProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 認證業務編排。
 * 處理使用者註冊、登入與 Token 刷新的業務流程。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthUseCase {

    private final UserManager userManager;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationService authenticationService;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    /**
     * 使用者註冊。
     * 檢查 Email 是否已存在，建立使用者並回傳 Token。
     *
     * @param request 註冊請求
     * @return 登入回應（含 Token 與使用者資料）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse register(@NonNull UserRegisterRequest request) {
        UserEntity savedUser = authenticationService.createUser(request, UserRole.USER);
        return authenticationService.issueTokens(savedUser);
    }

    /**
     * 使用者登入。
     * 驗證帳號密碼並回傳 Token。
     *
     * @param request 登入請求
     * @return 登入回應（含 Token 與使用者資料）
     */
    @Transactional(readOnly = true)
    public UserLoginResponse login(@NonNull UserLoginRequest request) {
        return authenticationService.issueTokens(authenticationService.authenticate(request));
    }

    /**
     * 刷新 Token。
     * 驗證 Refresh Token 並產生新的 Token 組。
     *
     * @param request Token 刷新請求
     * @return 登入回應（含新的 Token 與使用者資料）
     */
    @Transactional(readOnly = true)
    public UserLoginResponse refreshToken(@NonNull TokenRefreshRequest request) {
        UUID userId = jwtTokenService.getUserIdFromToken(request.getRefreshToken());

        UserEntity userEntity = userManager.findByIdOrThrow(userId);

        return authenticationService.issueTokens(userEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse bootstrapAdmin(@NonNull BootstrapAdminRequest request) {
        if (!StringUtils.hasText(bootstrapAdminProperties.secret())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Admin bootstrap is disabled");
        }

        if (!bootstrapAdminProperties.secret().equals(request.getBootstrapSecret())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Bootstrap secret is invalid");
        }

        if (userManager.existsByRole(UserRole.SYSTEM_ADMIN)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "System admin already exists");
        }

        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setName(request.getName());
        registerRequest.setEmail(request.getEmail());
        registerRequest.setPassword(request.getPassword());

        UserEntity savedUser = authenticationService.createUser(registerRequest, UserRole.SYSTEM_ADMIN);
        return authenticationService.issueTokens(savedUser);
    }
}

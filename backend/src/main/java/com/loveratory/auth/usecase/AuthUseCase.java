package com.loveratory.auth.usecase;

import com.loveratory.auth.dto.request.TokenRefreshRequest;
import com.loveratory.auth.dto.request.UserLoginRequest;
import com.loveratory.auth.dto.request.UserRegisterRequest;
import com.loveratory.auth.dto.response.UserLoginResponse;
import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.entity.UserRole;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.auth.service.JwtTokenService;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;

    /**
     * 使用者註冊。
     * 檢查 Email 是否已存在，建立使用者並回傳 Token。
     *
     * @param request 註冊請求
     * @return 登入回應（含 Token 與使用者資料）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse register(@NonNull UserRegisterRequest request) {
        if (userManager.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(request.getEmail());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setName(request.getName());
        userEntity.setRole(UserRole.USER);

        UserEntity savedUser = userManager.save(userEntity);

        String accessToken = jwtTokenService.generateAccessToken(
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(savedUser.getId());

        return UserLoginResponse.fromEntity(savedUser, accessToken, refreshToken);
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
        UserEntity userEntity = userManager.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenService.generateAccessToken(
                userEntity.getId(), userEntity.getEmail(), userEntity.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(userEntity.getId());

        return UserLoginResponse.fromEntity(userEntity, accessToken, refreshToken);
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

        String accessToken = jwtTokenService.generateAccessToken(
                userEntity.getId(), userEntity.getEmail(), userEntity.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(userEntity.getId());

        return UserLoginResponse.fromEntity(userEntity, accessToken, refreshToken);
    }
}
